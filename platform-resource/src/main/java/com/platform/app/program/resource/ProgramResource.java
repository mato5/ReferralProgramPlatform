package com.platform.app.program.resource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.platform.app.common.exception.FieldNotValidException;
import com.platform.app.common.json.JsonReader;
import com.platform.app.common.json.JsonUtils;
import com.platform.app.common.json.JsonWriter;
import com.platform.app.common.json.OperationResultJsonWriter;
import com.platform.app.common.model.HttpCode;
import com.platform.app.common.model.OperationResult;
import com.platform.app.common.model.PaginatedData;
import com.platform.app.common.model.ResourceMessage;
import com.platform.app.invitation.exception.InvitationServiceException;
import com.platform.app.platformUser.exception.UserNotFoundException;
import com.platform.app.platformUser.model.User;
import com.platform.app.platformUser.services.PlatformUserServices;
import com.platform.app.program.exception.AppNotFoundException;
import com.platform.app.program.exception.ProgramExistentException;
import com.platform.app.program.exception.ProgramNotFoundException;
import com.platform.app.program.exception.ProgramServiceException;
import com.platform.app.program.model.Application;
import com.platform.app.program.model.Program;
import com.platform.app.program.services.ApplicationServices;
import com.platform.app.program.services.ProgramServices;
import com.platform.app.user.resource.UserJsonConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static com.platform.app.common.model.StandardsOperationResults.*;

@Path("/programs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProgramResource {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final ResourceMessage RESOURCE_MESSAGE = new ResourceMessage("application");

    @Inject
    ProgramServices programServices;

    @Inject
    ProgramJsonConverter programJsonConverter;

    @Inject
    UserJsonConverter userJsonConverter;

    @Inject
    PlatformUserServices userServices;

    @Inject
    ApplicationServices applicationServices;

    @Context
    SecurityContext securityContext;

    @Context
    UriInfo uriInfo;

    @GET
    @Path("/{id}")
    @PermitAll
    public Response findById(@PathParam("id") Long id) {
        logger.debug("Find application by id: {}", id);
        Response.ResponseBuilder responseBuilder;
        try {
            Program program = programServices.findById(id);
            OperationResult result = OperationResult.success(programJsonConverter.convertToJsonElement(program));
            responseBuilder = Response.status(HttpCode.OK.getCode()).entity(OperationResultJsonWriter.toJson(result));
            logger.debug("Program found by id: {}", program);
        } catch (ProgramNotFoundException e) {
            logger.error("No application found for id", id);
            responseBuilder = Response.status(HttpCode.NOT_FOUND.getCode());
        }

        return responseBuilder.build();
    }

    @DELETE
    @Path("/{id}")
    @PermitAll
    public Response delete(@PathParam("id") Long id) {
        logger.debug("Delete program by id: {}", id);

        if (!isUserAllowed(securityContext.getUserPrincipal().getName(), id)) {
            return Response.status(HttpCode.FORBIDDEN.getCode()).build();
        }

        Response.ResponseBuilder responseBuilder;
        try {
            Program program = programServices.findById(id);
            programServices.delete(program);
            OperationResult result = OperationResult.success(programJsonConverter.convertToJsonElement(program));
            responseBuilder = Response.status(HttpCode.OK.getCode()).entity(OperationResultJsonWriter.toJson(result));
            logger.debug("Program deleted by id: {}", id);
        } catch (ProgramNotFoundException e) {
            logger.error("No program found for id", id);
            responseBuilder = Response.status(HttpCode.NOT_FOUND.getCode());
        }

        return responseBuilder.build();
    }

    @POST
    @PermitAll
    public Response create(String body) {
        logger.debug("Adding a new program with body {}", body);
        Program program = programJsonConverter.convertFrom(body);

        HttpCode httpCode = HttpCode.CREATED;
        OperationResult result;
        try {
            User admin = userServices.findByEmail(securityContext.getUserPrincipal().getName());
            program.setAdmins(new HashSet<>(Collections.singleton(admin)));
            program = programServices.create(program);
            result = OperationResult.success(JsonUtils.getJsonElementWithId(program.getId()));
        } catch (FieldNotValidException e) {
            httpCode = HttpCode.VALIDATION_ERROR;
            logger.error("One of the fields of the invitation is not valid", e);
            result = getOperationResultInvalidField(RESOURCE_MESSAGE, e);
        } catch (ProgramExistentException e) {
            httpCode = HttpCode.VALIDATION_ERROR;
            logger.error("There is already a program with the given name", e);
            result = getOperationResultExistent(RESOURCE_MESSAGE, "name");
        } catch (UserNotFoundException e) {
            httpCode = HttpCode.NOT_FOUND;
            logger.error("User not found");
            result = getOperationResultDependencyNotFound(RESOURCE_MESSAGE, "admin");
        }

        logger.debug("Returning the operation result after sending invitation: {}", result);
        return Response.status(httpCode.getCode()).entity(OperationResultJsonWriter.toJson(result)).build();
    }

    @GET
    @PermitAll
    public Response findAllPrograms() {
        logger.debug("Finding all programs.");

        List<Program> programs = programServices.findAll(null);

        logger.debug("Found {} programs", programs.size());

        JsonElement jsonWithPagingAndEntries = JsonUtils.getJsonElementWithPagingAndEntries(
                new PaginatedData<Program>(programs.size(), programs), programJsonConverter);
        return Response.status(HttpCode.OK.getCode()).entity(JsonWriter.writeToString(jsonWithPagingAndEntries))
                .build();
    }

    @GET
    @Path("/admin/{id}")
    @PermitAll
    public Response findByAdmin(@PathParam("id") Long adminId) {
        logger.debug("Finding all programs by admin ID: {}", adminId);
        if (!securityContext.isUserInRole(User.Roles.ADMINISTRATOR.name())) {
            if (!isLoggedUser(adminId)) {
                return Response.status(HttpCode.FORBIDDEN.getCode()).build();
            }
        }
        List<Program> programs;
        try {
            User admin = userServices.findById(adminId);
            programs = programServices.findByAdmin(admin);
        } catch (UserNotFoundException e) {
            logger.error("No user found");
            return Response.status(HttpCode.NOT_FOUND.getCode()).build();
        }

        logger.debug("Found {} programs", programs.size());

        JsonElement jsonWithPagingAndEntries = JsonUtils.getJsonElementWithPagingAndEntries(
                new PaginatedData<Program>(programs.size(), programs), programJsonConverter);
        return Response.status(HttpCode.OK.getCode()).entity(JsonWriter.writeToString(jsonWithPagingAndEntries))
                .build();
    }

    @GET
    @Path("/application/{apikey}")
    @PermitAll
    public Response findByApplication(@PathParam("apikey") String apiKey) {
        logger.debug("Finding program by its application");
        Response.ResponseBuilder responseBuilder;
        try {
            UUID id = UUID.fromString(apiKey);
            Application app = applicationServices.findByApiKey(id);
            Program program = programServices.findByApplication(app);
            OperationResult result = OperationResult.success(programJsonConverter.convertToJsonElement(program));
            responseBuilder = Response.status(HttpCode.OK.getCode()).entity(OperationResultJsonWriter.toJson(result));
            logger.debug("Program found by API key: {}", apiKey);
        } catch (AppNotFoundException e) {
            logger.error("No app found for API key: {}", apiKey);
            responseBuilder = Response.status(HttpCode.NOT_FOUND.getCode());
        } catch (ProgramNotFoundException e) {
            logger.error("No program found for API key: {}", apiKey);
            responseBuilder = Response.status(HttpCode.NOT_FOUND.getCode());
        } catch (IllegalArgumentException e) {
            logger.error("The API key provided is not a correct UUID representation: {}", apiKey);
            responseBuilder = Response.status(HttpCode.VALIDATION_ERROR.getCode());
        }

        return responseBuilder.build();
    }

    @GET
    @Path("/name/{name}")
    @PermitAll
    public Response findByName(@PathParam("name") String name) {
        logger.debug("Finding all programs by name: {}", name);
        Response.ResponseBuilder responseBuilder;
        try {
            Program program = programServices.findByName(name);
            OperationResult result = OperationResult.success(programJsonConverter.convertToJsonElement(program));
            responseBuilder = Response.status(HttpCode.OK.getCode()).entity(OperationResultJsonWriter.toJson(result));
            logger.debug("Program found by name: {}", name);
        } catch (ProgramNotFoundException e) {
            logger.error("No program found for name: {}", name);
            responseBuilder = Response.status(HttpCode.NOT_FOUND.getCode());
        }

        return responseBuilder.build();
    }

    @GET
    @Path("/user/{id}")
    @PermitAll
    public Response findByUser(@PathParam("id") Long id) {
        logger.debug("Finding all programs by user ID: {}", id);

        if (!securityContext.isUserInRole(User.Roles.ADMINISTRATOR.name())) {
            if (!isLoggedUser(id)) {
                return Response.status(HttpCode.FORBIDDEN.getCode()).build();
            }
        }

        List<Program> programs;
        try {
            User customer = userServices.findById(id);
            programs = programServices.findByActiveUser(customer);
        } catch (UserNotFoundException e) {
            logger.error("No user found");
            return Response.status(HttpCode.NOT_FOUND.getCode()).build();
        }

        logger.debug("Found {} programs", programs.size());

        JsonElement jsonWithPagingAndEntries = JsonUtils.getJsonElementWithPagingAndEntries(
                new PaginatedData<Program>(programs.size(), programs), programJsonConverter);
        return Response.status(HttpCode.OK.getCode()).entity(JsonWriter.writeToString(jsonWithPagingAndEntries))
                .build();
    }

    @PUT
    @Path("/{id}/name")
    @PermitAll
    public Response changeName(@PathParam("id") Long id, String body) {
        logger.debug("Changing name of a program ID: {}", id);

        if (!isUserAllowed(securityContext.getUserPrincipal().getName(), id)) {
            return Response.status(HttpCode.FORBIDDEN.getCode()).build();
        }

        HttpCode httpCode = HttpCode.OK;
        OperationResult result;
        try {
            programServices.changeName(getNameFromJson(body), id);
            result = OperationResult.success();
        } catch (ProgramExistentException e) {
            httpCode = HttpCode.VALIDATION_ERROR;
            logger.error("There is already a program with the given name", e);
            result = getOperationResultExistent(RESOURCE_MESSAGE, "name");
        } catch (ProgramNotFoundException e) {
            httpCode = HttpCode.NOT_FOUND;
            logger.error("The program with ID: {} was not found", id);
            result = getOperationResultNotFound(RESOURCE_MESSAGE);
        }
        logger.debug("Returning the operation result after updating program name: {}", result);
        return Response.status(httpCode.getCode()).entity(OperationResultJsonWriter.toJson(result)).build();
    }

    @PUT
    @Path("/{id}/add_admin")
    @PermitAll
    public Response addAdmin(@PathParam("id") Long id, String body) {
        logger.debug("Adding an admin to a program ID: {}", id);

        if (!isUserAllowed(securityContext.getUserPrincipal().getName(), id)) {
            return Response.status(HttpCode.FORBIDDEN.getCode()).build();
        }

        HttpCode httpCode = HttpCode.OK;
        OperationResult result;
        try {
            User admin = userJsonConverter.convertFrom(body);
            admin = userServices.findByEmail(admin.getEmail());
            programServices.addAdmin(admin.getId(), id);
            result = OperationResult.success();
        } catch (UserNotFoundException e) {
            httpCode = HttpCode.NOT_FOUND;
            logger.error("User was not found", e);
            result = getOperationResultNotFound(new ResourceMessage("User not found"));
        } catch (ProgramNotFoundException e) {
            httpCode = HttpCode.NOT_FOUND;
            logger.error("The program with ID: {} was not found", id);
            result = getOperationResultNotFound(RESOURCE_MESSAGE);
        } catch (ProgramServiceException e) {
            httpCode = HttpCode.VALIDATION_ERROR;
            logger.error("The user is already an admin of this program", id);
            result = getOperationResultExistent(RESOURCE_MESSAGE, "admins");
        }
        logger.debug("Returning the operation result after adding program administrator: {}", result);
        return Response.status(httpCode.getCode()).entity(OperationResultJsonWriter.toJson(result)).build();
    }

    @PUT
    @Path("/{id}/remove_admin")
    @PermitAll
    public Response removeAdmin(@PathParam("id") Long id, String body) {
        logger.debug("Removing an admin from a program ID: {}", id);

        if (!isLoggedAdmin(securityContext.getUserPrincipal().getName())) {
            return Response.status(HttpCode.FORBIDDEN.getCode()).build();
        }

        HttpCode httpCode = HttpCode.OK;
        OperationResult result;
        try {
            User admin = userJsonConverter.convertFrom(body);
            admin = userServices.findByEmail(admin.getEmail());
            programServices.removeAdmin(admin.getId(), id);
            result = OperationResult.success();
        } catch (UserNotFoundException e) {
            httpCode = HttpCode.NOT_FOUND;
            logger.error("User was not found", e);
            result = getOperationResultNotFound(new ResourceMessage("User not found"));
        } catch (ProgramNotFoundException e) {
            httpCode = HttpCode.NOT_FOUND;
            logger.error("The program with ID: {} was not found", id);
            result = getOperationResultNotFound(RESOURCE_MESSAGE);
        } catch (ProgramServiceException e) {
            httpCode = HttpCode.INTERNAL_ERROR;
            logger.error("Internal program service error when removing an admin: " + e.getMessage(), id);
            result = getOperationResultDependencyNotFound(RESOURCE_MESSAGE, e.getMessage());
        }
        logger.debug("Returning the operation result after adding program administrator: {}", result);
        return Response.status(httpCode.getCode()).entity(OperationResultJsonWriter.toJson(result)).build();
    }

    @PUT
    @Path("/{id}/add_customer")
    @PermitAll
    public Response addCustomer(@PathParam("id") Long id, String body) {
        logger.debug("Adding a customer to a program ID: {}", id);

        if (!isUserAllowed(securityContext.getUserPrincipal().getName(), id)) {
            return Response.status(HttpCode.FORBIDDEN.getCode()).build();
        }

        HttpCode httpCode = HttpCode.OK;
        OperationResult result;
        try {
            User customer = userJsonConverter.convertFrom(body);
            customer = userServices.findByEmail(customer.getEmail());
            programServices.addCustomer(customer.getId(), id);
            result = OperationResult.success();
        } catch (UserNotFoundException e) {
            httpCode = HttpCode.NOT_FOUND;
            logger.error("User was not found", e);
            result = getOperationResultNotFound(new ResourceMessage("User not found"));
        } catch (ProgramNotFoundException e) {
            httpCode = HttpCode.NOT_FOUND;
            logger.error("The program with ID: {} was not found", id);
            result = getOperationResultNotFound(RESOURCE_MESSAGE);
        } catch (ProgramServiceException e) {
            httpCode = HttpCode.VALIDATION_ERROR;
            logger.error("The user is already an active customer of this program", id);
            result = getOperationResultExistent(RESOURCE_MESSAGE, "activeCustomers");
        }
        logger.debug("Returning the operation result after adding program customer: {}", result);
        return Response.status(httpCode.getCode()).entity(OperationResultJsonWriter.toJson(result)).build();
    }

    @PUT
    @Path("/{id}/remove_customer")
    @PermitAll
    public Response removeCustomer(@PathParam("id") Long id, String body) {
        logger.debug("Removing a customer from a program ID: {}", id);

        if (!isUserAllowed(securityContext.getUserPrincipal().getName(), id)) {
            return Response.status(HttpCode.FORBIDDEN.getCode()).build();
        }

        HttpCode httpCode = HttpCode.OK;
        OperationResult result;
        try {
            User customer = userJsonConverter.convertFrom(body);
            customer = userServices.findByEmail(customer.getEmail());
            programServices.removeCustomer(customer.getId(), id);
            result = OperationResult.success();
        } catch (UserNotFoundException e) {
            httpCode = HttpCode.NOT_FOUND;
            logger.error("User was not found", e);
            result = getOperationResultNotFound(new ResourceMessage("User not found"));
        } catch (ProgramNotFoundException e) {
            httpCode = HttpCode.NOT_FOUND;
            logger.error("The program with ID: {} was not found", id);
            result = getOperationResultNotFound(RESOURCE_MESSAGE);
        } catch (ProgramServiceException e) {
            httpCode = HttpCode.VALIDATION_ERROR;
            logger.error("The user is not an active customer of this program", id);
            result = getOperationResultExistent(RESOURCE_MESSAGE, "activeCustomers");
        }
        logger.debug("Returning the operation result after removing program customer: {}", result);
        return Response.status(httpCode.getCode()).entity(OperationResultJsonWriter.toJson(result)).build();
    }

    @PUT
    @Path("/{id}/register")
    @PermitAll
    public Response registerOnWaitingList(@PathParam("id") Long id) {
        logger.debug("Register on the waiting list of program ID: {}", id);
        Response.ResponseBuilder responseBuilder;
        try {
            User toBeRegistered = userServices.findByEmail(securityContext.getUserPrincipal().getName());
            Program program = programServices.registerOnWaitingList(id, toBeRegistered.getId());
            OperationResult result = OperationResult.success(programJsonConverter.convertToJsonElement(program));
            responseBuilder = Response.status(HttpCode.OK.getCode()).entity(OperationResultJsonWriter.toJson(result));
            logger.debug("Registered on the waiting list of program ID: {}", id);
        } catch (ProgramNotFoundException e) {
            logger.error("Program not found: {}", e.getMessage());
            responseBuilder = Response.status(HttpCode.INTERNAL_ERROR.getCode());
        } catch (UserNotFoundException e) {
            logger.error("User not found");
            responseBuilder = Response.status(HttpCode.NOT_FOUND.getCode());
        } catch (ProgramServiceException e) {
            logger.error("Program Service Exception: " + e.getMessage());
            responseBuilder = Response.status(HttpCode.INTERNAL_ERROR.getCode());
        }

        return responseBuilder.build();
    }

    @PUT
    @Path("/{id}/unregister")
    @PermitAll
    public Response unregisterOnWaitingList(@PathParam("id") Long id) {
        logger.debug("Unregister on the waiting list of program ID: {}", id);
        Response.ResponseBuilder responseBuilder;
        try {
            User toBeRegistered = userServices.findByEmail(securityContext.getUserPrincipal().getName());
            Program program = programServices.unregisterOnWaitingList(id, toBeRegistered.getId());
            OperationResult result = OperationResult.success(programJsonConverter.convertToJsonElement(program));
            responseBuilder = Response.status(HttpCode.OK.getCode()).entity(OperationResultJsonWriter.toJson(result));
            logger.debug("Unregistered on the waiting list of program ID: {}", id);
        } catch (ProgramNotFoundException e) {
            logger.error("Program not found: {}", e.getMessage());
            responseBuilder = Response.status(HttpCode.INTERNAL_ERROR.getCode());
        } catch (UserNotFoundException e) {
            logger.error("User not found");
            responseBuilder = Response.status(HttpCode.NOT_FOUND.getCode());
        } catch (ProgramServiceException e) {
            logger.error("Program Service Exception: " + e.getMessage());
            responseBuilder = Response.status(HttpCode.INTERNAL_ERROR.getCode());
        }

        return responseBuilder.build();
    }

    @PUT
    @Path("/{id}/register_app/{apiKey}")
    @PermitAll
    public Response registerApp(@PathParam("id") Long programId, @PathParam("apiKey") String apiKey) {
        logger.debug("Registering an app to a program ID: {}", programId);

        if (!isUserAllowed(securityContext.getUserPrincipal().getName(), programId)) {
            return Response.status(HttpCode.FORBIDDEN.getCode()).build();
        }

        HttpCode httpCode = HttpCode.OK;
        OperationResult result;
        try {
            UUID id = UUID.fromString(apiKey);
            programServices.registerApplication(id, programId);
            result = OperationResult.success();
        } catch (AppNotFoundException e) {
            httpCode = HttpCode.NOT_FOUND;
            logger.error("App was not found", e);
            result = getOperationResultNotFound(new ResourceMessage("App not found"));
        } catch (ProgramNotFoundException e) {
            httpCode = HttpCode.NOT_FOUND;
            logger.error("The program with ID: {} was not found", programId);
            result = getOperationResultNotFound(RESOURCE_MESSAGE);
        } catch (ProgramServiceException e) {
            httpCode = HttpCode.VALIDATION_ERROR;
            logger.error("This program already contains the specified app {}", apiKey);
            result = getOperationResultExistent(RESOURCE_MESSAGE, "activeApplications");
        } catch (IllegalArgumentException e) {
            logger.error("The API key provided is not a correct UUID representation: {}", apiKey);
            httpCode = HttpCode.NOT_FOUND;
            result = getOperationResultNotFound(new ResourceMessage("App not found"));
        }
        logger.debug("Returning the operation result after registering program application: {}", result);
        return Response.status(httpCode.getCode()).entity(OperationResultJsonWriter.toJson(result)).build();
    }

    @PUT
    @Path("/{id}/unregister_app/{apiKey}")
    @PermitAll
    public Response unregisterApp(@PathParam("id") Long programId, @PathParam("apiKey") String apiKey) {
        logger.debug("Registering an app to a program ID: {}", programId);

        if (!isUserAllowed(securityContext.getUserPrincipal().getName(), programId)) {
            return Response.status(HttpCode.FORBIDDEN.getCode()).build();
        }

        HttpCode httpCode = HttpCode.OK;
        OperationResult result;
        try {
            UUID id = UUID.fromString(apiKey);
            programServices.unregisterApplication(id, programId);
            result = OperationResult.success();
        } catch (AppNotFoundException e) {
            httpCode = HttpCode.NOT_FOUND;
            logger.error("App was not found", e);
            result = getOperationResultNotFound(new ResourceMessage("App not found"));
        } catch (ProgramNotFoundException e) {
            httpCode = HttpCode.NOT_FOUND;
            logger.error("The program with ID: {} was not found", programId);
            result = getOperationResultNotFound(RESOURCE_MESSAGE);
        } catch (ProgramServiceException e) {
            httpCode = HttpCode.VALIDATION_ERROR;
            logger.error("This program does not contain the specified app {}", apiKey);
            result = getOperationResultExistent(RESOURCE_MESSAGE, "activeApplications");
        } catch (IllegalArgumentException e) {
            logger.error("The API key provided is not a correct UUID representation: {}", apiKey);
            httpCode = HttpCode.NOT_FOUND;
            result = getOperationResultNotFound(new ResourceMessage("App not found"));
        }
        logger.debug("Returning the operation result after unregistering program application: {}", result);
        return Response.status(httpCode.getCode()).entity(OperationResultJsonWriter.toJson(result)).build();
    }

    @PUT
    @Path("/{id}/invite_waitinglist/{amount}")
    @PermitAll
    public Response inviteFromWaitingList(@PathParam("id") Long id, @PathParam("amount") Integer allowedInvitations, String body) {
        logger.debug("Inviting from a waiting list of program ID: {}", id);

        if (!isUserAllowed(securityContext.getUserPrincipal().getName(), id)) {
            return Response.status(HttpCode.FORBIDDEN.getCode()).build();
        }

        HttpCode httpCode = HttpCode.OK;
        OperationResult result;
        try {
            User admin = userServices.findByEmail(securityContext.getUserPrincipal().getName());
            List<Long> ids = userJsonConverter.convertIds(body);
            programServices.inviteFromWaitingList(admin.getId(), id, ids, allowedInvitations);
            result = OperationResult.success();
        } catch (ProgramNotFoundException e) {
            httpCode = HttpCode.NOT_FOUND;
            logger.error("Program was not found", e);
            result = getOperationResultNotFound(RESOURCE_MESSAGE);
        } catch (UserNotFoundException e) {
            httpCode = HttpCode.NOT_FOUND;
            logger.error("User was not found", e);
            result = getOperationResultNotFound(new ResourceMessage("User not found"));
        } catch (ProgramServiceException e) {
            httpCode = HttpCode.INTERNAL_ERROR;
            logger.error("Program service exception: " + e.getMessage(), e);
            result = getOperationResultExistent(RESOURCE_MESSAGE, e.getMessage());
        } catch (InvitationServiceException e) {
            httpCode = HttpCode.INTERNAL_ERROR;
            logger.error("Invitation  service exception: " + e.getMessage(), e);
            result = getOperationResultExistent(RESOURCE_MESSAGE, e.getMessage());
        }
        logger.debug("Returning the operation result after inviting from the waiting list: {}", result);
        return Response.status(httpCode.getCode()).entity(OperationResultJsonWriter.toJson(result)).build();
    }

    @PUT
    @Path("/{name}/role")
    @PermitAll
    public Response getUsersRole(@PathParam("name") String programName, String body) {
        logger.debug("Get users role in program name: {}", programName);
        Response.ResponseBuilder responseBuilder;
        User.Roles role;
        try {
            User user = userServices.findByEmail(getEmailFromJson(body));
            Program program = programServices.findByName(programName);
            role = programServices.getUsersRole(user, program);
            logger.debug("Role found for program name: {}", programName);
        } catch (ProgramNotFoundException e) {
            logger.error("No program found for name: {}", programName);
            role = User.Roles.NONE;
        } catch (UserNotFoundException e) {
            logger.error("No user found for email: {}", body);
            role = User.Roles.NONE;
        } catch (Exception e) {
            role = User.Roles.NONE;
        }
        OperationResult result = OperationResult.success(programJsonConverter.convertRoleToJson(role));
        responseBuilder = Response.status(HttpCode.OK.getCode()).entity(OperationResultJsonWriter.toJson(result));
        return responseBuilder.build();
    }

    private boolean isLoggedAdmin(String email) {
        try {
            User loggerUser = userServices.findByEmail(email);
            if (loggerUser.getUserType().equals(User.UserType.EMPLOYEE)) {
                return true;
            }
        } catch (UserNotFoundException e) {
            return false;
        }
        return false;
    }

    private boolean isUserAllowed(String email, Long programId) {
        try {
            if (!User.Roles.ADMINISTRATOR.equals(programServices.getUsersRole(email, programId))) {
                if (!isLoggedAdmin(email)) {
                    return false;
                }
            }
        } catch (UserNotFoundException | ProgramNotFoundException e) {
            return false;
        }
        return true;
    }

    private boolean isLoggedUser(final Long id) {
        try {
            final User loggerUser = userServices.findByEmail(securityContext.getUserPrincipal().getName());
            if (loggerUser.getId().equals(id)) {
                return true;
            }
        } catch (final UserNotFoundException e) {
        }
        return false;
    }


    private String getNameFromJson(final String body) {
        final JsonObject jsonObject = JsonReader.readAsJsonObject(body);
        return JsonReader.getStringOrNull(jsonObject, "name");
    }

    private String getEmailFromJson(final String body) {
        final JsonObject jsonObject = JsonReader.readAsJsonObject(body);
        return JsonReader.getStringOrNull(jsonObject, "email");
    }
}

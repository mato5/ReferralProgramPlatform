package com.platform.app.invitation.resource;

import com.google.gson.JsonArray;
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
import com.platform.app.geoIP.model.GeoIP;
import com.platform.app.invitation.exception.InvitationNotFoundException;
import com.platform.app.invitation.exception.InvitationServiceException;
import com.platform.app.invitation.model.Invitation;
import com.platform.app.invitation.services.InvitationServices;
import com.platform.app.platformUser.exception.UserNotFoundException;
import com.platform.app.platformUser.model.User;
import com.platform.app.platformUser.services.PlatformUserServices;
import com.platform.app.program.exception.ProgramNotFoundException;
import com.platform.app.program.model.Program;
import com.platform.app.program.services.ProgramServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.platform.app.common.model.StandardsOperationResults.getOperationResultDependencyNotFound;
import static com.platform.app.common.model.StandardsOperationResults.getOperationResultInvalidField;

@Path("/invitations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InvitationResource {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final ResourceMessage RESOURCE_MESSAGE = new ResourceMessage("invitation");

    @Inject
    InvitationServices invitationServices;

    @Inject
    ProgramServices programServices;

    @Inject
    PlatformUserServices userServices;

    @Inject
    InvitationJsonConverter invitationJsonConverter;

    @Inject
    GeoIPJsonConverter geoIPJsonConverter;

    @Context
    SecurityContext securityContext;

    @Context
    UriInfo uriInfo;


    @POST
    @PermitAll
    public Response send(String body) {
        logger.debug("Adding a new invitation with body {}", body);
        Invitation invitation = invitationJsonConverter.convertFrom(body);

        if (!userCanInvite(securityContext.getUserPrincipal().getName(), invitation.getProgramId())) {
            return Response.status(HttpCode.FORBIDDEN.getCode()).build();
        }

        HttpCode httpCode = HttpCode.CREATED;
        OperationResult result;
        try {
            User invitedBy = userServices.findByEmail(securityContext.getUserPrincipal().getName());
            invitation.setByUserId(invitedBy.getId());
            invitation = invitationServices.send(invitation);
            result = OperationResult.success(JsonUtils.getJsonElementWithId(invitation.getId()));
        } catch (FieldNotValidException e) {
            httpCode = HttpCode.VALIDATION_ERROR;
            logger.error("One of the fields of the invitation is not valid", e);
            result = getOperationResultInvalidField(RESOURCE_MESSAGE, e);
        } catch (ProgramNotFoundException e) {
            logger.error("Program not found");
            httpCode = HttpCode.NOT_FOUND;
            result = getOperationResultDependencyNotFound(RESOURCE_MESSAGE, "program");
        } catch (UserNotFoundException e) {
            logger.error("User not found");
            httpCode = HttpCode.NOT_FOUND;
            result = getOperationResultDependencyNotFound(RESOURCE_MESSAGE, "user");
        } catch (InvitationServiceException e) {
            logger.error("Invitation service error: {}", e.getMessage());
            httpCode = HttpCode.INTERNAL_ERROR;
            result = OperationResult.error("Invitation service error", e.getMessage());
        }

        logger.debug("Returning the operation result after sending invitation: {}", result);
        return Response.status(httpCode.getCode()).entity(OperationResultJsonWriter.toJson(result)).build();
    }

    @POST
    @Path("/batch")
    @PermitAll
    public Response sendInBatch(String body) {
        logger.debug("Sending invitations in a batch with body {}", body);
        Long programId = getProgramIdFromJson(body);

        if (!isUserAllowed(securityContext.getUserPrincipal().getName(), programId)) {
            return Response.status(HttpCode.FORBIDDEN.getCode()).build();
        }

        Integer allowedInvitations = getInvitationsLeft(body);
        List<String> emails = getEmails(body);
        HttpCode httpCode = HttpCode.CREATED;
        OperationResult result;
        try {
            List<Invitation> invitations = invitationServices.sendInBatch(securityContext.getUserPrincipal().getName(),
                    programId, emails, allowedInvitations);
            result = OperationResult.
                    success(JsonUtils.getJsonElementWithPagingAndEntries(new PaginatedData<>(invitations.size(), invitations),
                            invitationJsonConverter));
        } catch (UserNotFoundException u) {
            logger.error("User not found");
            httpCode = HttpCode.NOT_FOUND;
            result = getOperationResultDependencyNotFound(RESOURCE_MESSAGE, "user");
        } catch (InvitationServiceException i) {
            logger.error("Invitation service error: {}", i.getMessage());
            httpCode = HttpCode.INTERNAL_ERROR;
            result = OperationResult.error("Invitation service error", i.getMessage());
        } catch (ProgramNotFoundException p) {
            logger.error("Program not found");
            httpCode = HttpCode.NOT_FOUND;
            result = getOperationResultDependencyNotFound(RESOURCE_MESSAGE, "program");
        } catch (FieldNotValidException f) {
            httpCode = HttpCode.VALIDATION_ERROR;
            logger.error("One of the fields of the author is not valid", f);
            result = getOperationResultInvalidField(RESOURCE_MESSAGE, f);
        }

        logger.debug("Returning the operation result after sending invitations in batch: {}", result);
        return Response.status(httpCode.getCode()).entity(OperationResultJsonWriter.toJson(result)).build();
    }

    @GET
    @Path("/{id}")
    @PermitAll
    public Response findById(@PathParam("id") final Long id) {
        logger.debug("Find invitation by id: {}", id);
        Response.ResponseBuilder responseBuilder;
        try {
            Invitation invitation = invitationServices.findById(id);
            OperationResult result = OperationResult.success(invitationJsonConverter.convertToJsonElement(invitation));
            responseBuilder = Response.status(HttpCode.OK.getCode()).entity(OperationResultJsonWriter.toJson(result));
            logger.debug("Invitation found by id: {}", invitation);
        } catch (InvitationNotFoundException e) {
            logger.error("No invitation found for id", id);
            responseBuilder = Response.status(HttpCode.NOT_FOUND.getCode());
        }

        return responseBuilder.build();
    }

    @GET
    @PermitAll
    public Response findMyInvitations() {
        logger.debug("Finding invitations of the active user");

        List<Invitation> invitations = invitationServices.findByInvitee(securityContext.getUserPrincipal().getName());

        logger.debug("Found {} invitations", invitations.size());

        JsonElement jsonWithPagingAndEntries = JsonUtils.getJsonElementWithPagingAndEntries(
                new PaginatedData<Invitation>(invitations.size(), invitations), invitationJsonConverter);
        return Response.status(HttpCode.OK.getCode()).entity(JsonWriter.writeToString(jsonWithPagingAndEntries))
                .build();
    }

    @GET
    @Path("/program/{id}/left")
    @PermitAll
    public Response getInvitationsLeft(@PathParam("id") Long id) {
        logger.debug("Finding invitations left of the active user");
        try {
            User user = userServices.findByEmail(securityContext.getUserPrincipal().getName());
            int left = invitationServices.findInvitationsLeft(user.getId(), id);
            return Response.status(HttpCode.OK.getCode()).entity(left).build();
        } catch (Exception e) {
            return Response.status(HttpCode.NOT_FOUND.getCode()).build();
        }
    }

    @GET
    @Path("/sent")
    @PermitAll
    public Response findSentInvitations() {

        logger.debug("Finding invitations sent by the active user");

        List<Invitation> invitations = invitationServices.findByInvitor(securityContext.getUserPrincipal().getName());

        logger.debug("Found {} invitations", invitations.size());

        JsonElement jsonWithPagingAndEntries = JsonUtils.getJsonElementWithPagingAndEntries(
                new PaginatedData<Invitation>(invitations.size(), invitations), invitationJsonConverter);
        return Response.status(HttpCode.OK.getCode()).entity(JsonWriter.writeToString(jsonWithPagingAndEntries))
                .build();

    }

    @GET
    @Path("/program/{id}")
    @PermitAll
    public Response findByProgram(@PathParam("id") Long id) {
        logger.debug("Finding invitations by program id: {}", id);

        if (!isUserAllowed(securityContext.getUserPrincipal().getName(), id)) {
            return Response.status(HttpCode.FORBIDDEN.getCode()).build();
        }

        List<Invitation> invitations = invitationServices.findByProgram(id);

        logger.debug("Found {} invitations", invitations.size());

        JsonElement jsonWithPagingAndEntries = JsonUtils.getJsonElementWithPagingAndEntries(
                new PaginatedData<Invitation>(invitations.size(), invitations), invitationJsonConverter);
        return Response.status(HttpCode.OK.getCode()).entity(JsonWriter.writeToString(jsonWithPagingAndEntries))
                .build();
    }

    @GET
    @Path("/all")
    @PermitAll
    public Response findAllInvitations() {
        logger.debug("Finding all invitations.");

        List<Invitation> invitations = invitationServices.findALl(null);

        logger.debug("Found {} invitations", invitations.size());

        JsonElement jsonWithPagingAndEntries = JsonUtils.getJsonElementWithPagingAndEntries(
                new PaginatedData<Invitation>(invitations.size(), invitations), invitationJsonConverter);
        return Response.status(HttpCode.OK.getCode()).entity(JsonWriter.writeToString(jsonWithPagingAndEntries))
                .build();
    }

    @DELETE
    @Path("/{id}")
    @PermitAll
    public Response delete(@PathParam("id") Long id) {
        logger.debug("Delete invitation by id: {}", id);

        Response.ResponseBuilder responseBuilder;
        try {
            Invitation invitation = invitationServices.findById(id);
            invitationServices.delete(invitation);
            OperationResult result = OperationResult.success(invitationJsonConverter.convertToJsonElement(invitation));
            responseBuilder = Response.status(HttpCode.OK.getCode()).entity(OperationResultJsonWriter.toJson(result));
            logger.debug("Invitation deleted by id: {}", id);
        } catch (InvitationNotFoundException e) {
            logger.error("No invitation found for id", id);
            responseBuilder = Response.status(HttpCode.NOT_FOUND.getCode());
        }

        return responseBuilder.build();
    }

    @PUT
    @Path("/{id}/decline")
    @PermitAll
    public Response decline(@PathParam("id") Long id) {
        logger.debug("Decline invitation by id: {}", id);

        if (!userCanAcceptDecline(securityContext.getUserPrincipal().getName(), id)) {
            return Response.status(HttpCode.FORBIDDEN.getCode()).build();
        }

        Response.ResponseBuilder responseBuilder;
        try {
            Invitation invitation = invitationServices.findById(id);
            invitationServices.decline(invitation);
            invitation.setDeclined(true);
            OperationResult result = OperationResult.success(invitationJsonConverter.convertToJsonElement(invitation));
            responseBuilder = Response.status(HttpCode.OK.getCode()).entity(OperationResultJsonWriter.toJson(result));
            logger.debug("Invitation decline by id: {}", id);
        } catch (InvitationServiceException e) {
            logger.error("Invitation service error: {}", e.getMessage());
            responseBuilder = Response.status(HttpCode.INTERNAL_ERROR.getCode());
        } catch (UserNotFoundException e) {
            logger.error("User not found");
            responseBuilder = Response.status(HttpCode.NOT_FOUND.getCode());
        } catch (ProgramNotFoundException e) {
            logger.error("Program not found");
            responseBuilder = Response.status(HttpCode.NOT_FOUND.getCode());
        } catch (InvitationNotFoundException e) {
            logger.error("Invitation not found");
            responseBuilder = Response.status(HttpCode.NOT_FOUND.getCode());
        }

        return responseBuilder.build();
    }

    @PUT
    @Path("/{id}/accept")
    @PermitAll
    public Response accept(@PathParam("id") Long id, String body) {
        logger.debug("Accept invitation by id: {} location: {}", id, body);

        if (!userCanAcceptDecline(securityContext.getUserPrincipal().getName(), id)) {
            return Response.status(HttpCode.FORBIDDEN.getCode()).build();
        }

        Response.ResponseBuilder responseBuilder;
        try {
            GeoIP location = geoIPJsonConverter.convertFrom(body);
            Invitation invitation = invitationServices.findById(id);
            invitationServices.accept(invitation, location);
            invitation = invitationServices.findById(id);
            OperationResult result = OperationResult.success(invitationJsonConverter.convertToJsonElement(invitation));
            responseBuilder = Response.status(HttpCode.OK.getCode()).entity(OperationResultJsonWriter.toJson(result));
            logger.debug("Invitation accept by id: {}", id);
        } catch (InvitationServiceException e) {
            logger.error("Invitation service error: {}", e.getMessage());
            responseBuilder = Response.status(HttpCode.INTERNAL_ERROR.getCode());
        } catch (UserNotFoundException e) {
            logger.error("User not found");
            responseBuilder = Response.status(HttpCode.NOT_FOUND.getCode());
        } catch (ProgramNotFoundException e) {
            logger.error("Program not found");
            responseBuilder = Response.status(HttpCode.NOT_FOUND.getCode());
        } catch (InvitationNotFoundException e) {
            logger.error("Invitation not found");
            responseBuilder = Response.status(HttpCode.NOT_FOUND.getCode());
        }

        return responseBuilder.build();
    }

    @GET
    @Path("/chart/{prog_id}")
    @PermitAll
    public Response getTreeChart(@PathParam("prog_id") Long programId) {
        logger.debug("Retrieving tree chart from program ID: {}", programId);
        if (!isUserAllowed(securityContext.getUserPrincipal().getName(), programId)) {
            return Response.status(HttpCode.FORBIDDEN.getCode()).build();
        }
        Response.ResponseBuilder responseBuilder;
        try {
            Program program = programServices.findById(programId);
            List<Invitation> invitations = invitationServices.findByProgram(programId);
            Set<User> users = new HashSet<>(program.getAdmins());
            for (Invitation inv : invitations) {
                User by = userServices.findById(inv.getByUserId());
                User to = userServices.findById(inv.getToUserId());
                users.add(by);
                users.add(to);
            }
            OperationResult result = OperationResult.success(invitationJsonConverter.getTreeChart(invitations, users, program.getName()));
            responseBuilder = Response.status(HttpCode.OK.getCode()).entity(OperationResultJsonWriter.toJson(result));
        } catch (ProgramNotFoundException e) {
            logger.error("Program not found");
            responseBuilder = Response.status(HttpCode.NOT_FOUND.getCode());
        } catch (UserNotFoundException e) {
            logger.error("User not found");
            responseBuilder = Response.status(HttpCode.NOT_FOUND.getCode());
        }

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

    private boolean userCanInvite(String email, Long programId) {
        try {
            User.Roles role = programServices.getUsersRole(securityContext.getUserPrincipal().getName(), programId);
            if (role == User.Roles.NONE) {
                if (!isLoggedAdmin(email)) {
                    return false;
                }
            }
        } catch (UserNotFoundException | ProgramNotFoundException e) {
            return false;
        }
        return true;
    }

    private boolean userCanAcceptDecline(String email, Long invId) {
        try {
            User user = userServices.findByEmail(email);
            Invitation invitation = invitationServices.findById(invId);
            if (!user.getId().equals(invitation.getToUserId())) {
                if (!isLoggedAdmin(email)) {
                    return false;
                }
            }
            return true;
        } catch (InvitationNotFoundException | UserNotFoundException e) {
            return false;
        }
    }

    private Long getProgramIdFromJson(String body) {
        final JsonObject jsonObject = JsonReader.readAsJsonObject(body);
        return JsonReader.getLongOrNull(jsonObject, "programId");
    }

    private Integer getInvitationsLeft(String body) {
        final JsonObject jsonObject = JsonReader.readAsJsonObject(body);
        return JsonReader.getIntegerOrNull(jsonObject, "invitationsLeft");
    }

    private List<String> getEmails(String body) {
        final JsonObject jsonObject = JsonReader.readAsJsonObject(body);
        JsonArray jsonArray = jsonObject.getAsJsonArray("emails");
        List<String> emails = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            emails.add(jsonArray.get(i).getAsString());
        }
        return emails;
    }

}

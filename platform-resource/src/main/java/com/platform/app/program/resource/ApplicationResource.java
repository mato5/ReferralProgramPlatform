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
import com.platform.app.platformUser.exception.UserNotFoundException;
import com.platform.app.platformUser.model.User;
import com.platform.app.platformUser.services.PlatformUserServices;
import com.platform.app.program.exception.AppExistentException;
import com.platform.app.program.exception.AppNotFoundException;
import com.platform.app.program.exception.AppServiceException;
import com.platform.app.program.model.Application;
import com.platform.app.program.services.ApplicationServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;
import java.util.UUID;

import static com.platform.app.common.model.StandardsOperationResults.*;

@Path("/applications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApplicationResource {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final ResourceMessage RESOURCE_MESSAGE = new ResourceMessage("application");

    @Inject
    ApplicationServices applicationServices;

    @Inject
    PlatformUserServices userServices;

    @Inject
    ApplicationJsonConverter applicationJsonConverter;

    @Context
    SecurityContext securityContext;

    @Context
    UriInfo uriInfo;

    @POST
    public Response create(String body) {
        logger.debug("Adding a new application with body {}", body);
        Application application = applicationJsonConverter.convertFrom(body);

        HttpCode httpCode = HttpCode.CREATED;
        OperationResult result;
        try {
            application = applicationServices.create(application);
            result = OperationResult.success(JsonUtils.getJsonElementWithId(application.getApiKey()));
        } catch (FieldNotValidException e) {
            httpCode = HttpCode.VALIDATION_ERROR;
            logger.error("One of the fields of the application is not valid", e);
            result = getOperationResultInvalidField(RESOURCE_MESSAGE, e);
        } catch (AppExistentException e) {
            httpCode = HttpCode.VALIDATION_ERROR;
            logger.error("There is already an application for the given URL", e);
            result = getOperationResultExistent(RESOURCE_MESSAGE, "URL");
        }

        logger.debug("Returning the operation result after creating an application: {}", result);
        return Response.status(httpCode.getCode()).entity(OperationResultJsonWriter.toJson(result)).build();
    }

    @DELETE
    @Path("/{id}")
    @PermitAll
    public Response delete(@PathParam("id") String id) {
        logger.debug("Delete application by id: {}", id);
        Response.ResponseBuilder responseBuilder;
        try {
            UUID uuid = UUID.fromString(id);
            if (!isUserAllowed(securityContext.getUserPrincipal().getName(), uuid)) {
                return Response.status(HttpCode.FORBIDDEN.getCode()).build();
            }
            Application app = applicationServices.findByApiKey(uuid);
            applicationServices.delete(app);
            OperationResult result = OperationResult.success(applicationJsonConverter.convertToJsonElement(app));
            responseBuilder = Response.status(HttpCode.OK.getCode()).entity(OperationResultJsonWriter.toJson(result));
            logger.debug("Application deleted by id: {}", id);
        } catch (AppNotFoundException e) {
            logger.error("No application found for id", id);
            responseBuilder = Response.status(HttpCode.NOT_FOUND.getCode());
        } catch (IllegalArgumentException e) {
            logger.error("The id provided is not a correct UUID representation: {}", id);
            responseBuilder = Response.status(HttpCode.VALIDATION_ERROR.getCode());
        }

        return responseBuilder.build();
    }

    @GET
    @RolesAllowed({"ADMINISTRATOR"})
    public Response findAllApps() {
        logger.debug("Finding all applications.");

        List<Application> apps = applicationServices.findAll(null);

        logger.debug("Found {} applications", apps.size());

        JsonElement jsonWithPagingAndEntries = JsonUtils.getJsonElementWithPagingAndEntries(
                new PaginatedData<Application>(apps.size(), apps), applicationJsonConverter);
        return Response.status(HttpCode.OK.getCode()).entity(JsonWriter.writeToString(jsonWithPagingAndEntries))
                .build();
    }

    @GET
    @Path("/user/{id}")
    @PermitAll
    public Response findAllAppsOfUser(@PathParam("id") Long userId) {
        logger.debug("Finding all applications of user ID: {}", userId);

        if (!isLoggedUser(userId) && !isLoggedAdmin(securityContext.getUserPrincipal().getName())) {
            return Response.status(HttpCode.FORBIDDEN.getCode()).build();
        }

        List<Application> apps;
        try {
            apps = applicationServices.findAllAppsOfUser(userId);
            logger.debug("Found {} applications", apps.size());
        } catch (UserNotFoundException e) {
            logger.error("No user found for id: {}", userId);
            OperationResult result = OperationResult
                    .error("User not found", "No user found for ID:" + userId);
            return Response.status(HttpCode.NOT_FOUND.getCode()).entity(OperationResultJsonWriter.toJson(result)).build();
        }

        JsonElement jsonWithPagingAndEntries = JsonUtils.getJsonElementWithPagingAndEntries(
                new PaginatedData<Application>(apps.size(), apps), applicationJsonConverter);
        return Response.status(HttpCode.OK.getCode()).entity(JsonWriter.writeToString(jsonWithPagingAndEntries))
                .build();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMINISTRATOR"})
    public Response findById(@PathParam("id") final String id) {
        logger.debug("Find application by id: {}", id);
        Response.ResponseBuilder responseBuilder;
        try {
            UUID uuid = UUID.fromString(id);
            if (!isUserAllowed(securityContext.getUserPrincipal().getName(), uuid)) {
                return Response.status(HttpCode.FORBIDDEN.getCode()).build();
            }
            Application application = applicationServices.findByApiKey(uuid);
            OperationResult result = OperationResult.success(applicationJsonConverter.convertToJsonElement(application));
            responseBuilder = Response.status(HttpCode.OK.getCode()).entity(OperationResultJsonWriter.toJson(result));
            logger.debug("Application found by id: {}", application);
        } catch (AppNotFoundException e) {
            logger.error("No application found for id", id);
            responseBuilder = Response.status(HttpCode.NOT_FOUND.getCode());
        } catch (IllegalArgumentException e) {
            logger.error("The id provided is not a correct UUID representation: {}", id);
            responseBuilder = Response.status(HttpCode.VALIDATION_ERROR.getCode());
        }

        return responseBuilder.build();
    }

    @PUT
    @Path("/url")
    public Response findByUrl(String body) {
        logger.debug("Find invitation by URL: {}", body);
        Response.ResponseBuilder responseBuilder;
        try {
            Application application = applicationServices.findByURL(getURLFromJson(body));
            OperationResult result = OperationResult.success(applicationJsonConverter.convertToJsonElement(application));
            responseBuilder = Response.status(HttpCode.OK.getCode()).entity(OperationResultJsonWriter.toJson(result));
            logger.debug("Application found by url: {}", application);
        } catch (AppNotFoundException e) {
            logger.error("No application found for url", body);
            responseBuilder = Response.status(HttpCode.NOT_FOUND.getCode());
        }

        return responseBuilder.build();
    }

    @GET
    @Path("/name/{name}")
    public Response findByName(@PathParam("name") String appName) {
        logger.debug("Finding all applications with a name: {}", appName);

        List<Application> apps = applicationServices.findByName(appName);

        logger.debug("Found {} applications", apps.size());

        JsonElement jsonWithPagingAndEntries = JsonUtils.getJsonElementWithPagingAndEntries(
                new PaginatedData<Application>(apps.size(), apps), applicationJsonConverter);
        return Response.status(HttpCode.OK.getCode()).entity(JsonWriter.writeToString(jsonWithPagingAndEntries))
                .build();
    }

    @PUT
    @Path("/{id}/name")
    public Response changeName(@PathParam("id") String uuid, String body) {
        logger.debug("Changing name of an app ID: {}", uuid);
        HttpCode httpCode = HttpCode.OK;
        OperationResult result;
        try {
            if (!isUserAllowed(securityContext.getUserPrincipal().getName(), UUID.fromString(uuid))) {
                return Response.status(HttpCode.FORBIDDEN.getCode()).build();
            }
            applicationServices.changeName(getNameFromJson(body), UUID.fromString(uuid));
            result = OperationResult.success();
        } catch (final AppNotFoundException e) {
            httpCode = HttpCode.NOT_FOUND;
            logger.error("No application found for the given id", e);
            result = getOperationResultNotFound(RESOURCE_MESSAGE);
        } catch (FieldNotValidException e) {
            httpCode = HttpCode.VALIDATION_ERROR;
            logger.error("The application name cannot be null value");
            result = getOperationResultInvalidField(RESOURCE_MESSAGE, e);
        } catch (IllegalArgumentException e) {
            httpCode = HttpCode.FORBIDDEN;
            logger.error("The id provided is not a correct UUID representation: {}", uuid);
            result = getOperationResultNotFound(RESOURCE_MESSAGE);
        }
        logger.debug("Returning the operation result after updating application name: {}", result);
        return Response.status(httpCode.getCode()).entity(OperationResultJsonWriter.toJson(result)).build();
    }

    @PUT
    @Path("/{id}/description")
    public Response changeDescription(@PathParam("id") String uuid, String body) {
        logger.debug("Changing description of an app ID: {}", uuid);
        HttpCode httpCode = HttpCode.OK;
        OperationResult result;
        try {
            if (!isUserAllowed(securityContext.getUserPrincipal().getName(), UUID.fromString(uuid))) {
                return Response.status(HttpCode.FORBIDDEN.getCode()).build();
            }
            applicationServices.changeDescription(getDescriptionFromJson(body), UUID.fromString(uuid));
            result = OperationResult.success();
        } catch (final AppNotFoundException e) {
            httpCode = HttpCode.NOT_FOUND;
            logger.error("No application found for the given id", e);
            result = getOperationResultNotFound(RESOURCE_MESSAGE);
        } catch (IllegalArgumentException e) {
            httpCode = HttpCode.FORBIDDEN;
            logger.error("The id provided is not a correct UUID representation: {}", uuid);
            result = getOperationResultNotFound(RESOURCE_MESSAGE);
        }
        logger.debug("Returning the operation result after updating application description: {}", result);
        return Response.status(httpCode.getCode()).entity(OperationResultJsonWriter.toJson(result)).build();
    }

    @PUT
    @Path("/{id}/url")
    public Response changeURL(@PathParam("id") String uuid, String body) {
        logger.debug("Changing URL of an app ID: {}", uuid);
        HttpCode httpCode = HttpCode.OK;
        OperationResult result;
        try {
            if (!isUserAllowed(securityContext.getUserPrincipal().getName(), UUID.fromString(uuid))) {
                return Response.status(HttpCode.FORBIDDEN.getCode()).build();
            }
            applicationServices.changeURL(getURLFromJson(body), UUID.fromString(uuid));
            result = OperationResult.success();
        } catch (final AppNotFoundException e) {
            httpCode = HttpCode.NOT_FOUND;
            logger.error("No application found for the given id", e);
            result = getOperationResultNotFound(RESOURCE_MESSAGE);
        } catch (AppServiceException e) {
            httpCode = HttpCode.INTERNAL_ERROR;
            logger.error("The application with a provided URL already exists.");
            result = getOperationResultExistent(RESOURCE_MESSAGE, "URL");
        } catch (IllegalArgumentException e) {
            httpCode = HttpCode.FORBIDDEN;
            logger.error("The id provided is not a correct UUID representation: {}", uuid);
            result = getOperationResultNotFound(RESOURCE_MESSAGE);
        }
        logger.debug("Returning the operation result after updating application URL: {}", result);
        return Response.status(httpCode.getCode()).entity(OperationResultJsonWriter.toJson(result)).build();
    }

    @PUT
    @Path("/{id}/invitation_url")
    public Response changeInvitationURL(@PathParam("id") String uuid, String body) {
        logger.debug("Changing invitation URL of an app ID: {}", uuid);
        HttpCode httpCode = HttpCode.OK;
        OperationResult result;
        try {
            if (!isUserAllowed(securityContext.getUserPrincipal().getName(), UUID.fromString(uuid))) {
                return Response.status(HttpCode.FORBIDDEN.getCode()).build();
            }
            applicationServices.changeInvitationURL(getInvitationURLFromJson(body), UUID.fromString(uuid));
            result = OperationResult.success();
        } catch (final AppNotFoundException e) {
            httpCode = HttpCode.NOT_FOUND;
            logger.error("No application found for the given id", e);
            result = getOperationResultNotFound(RESOURCE_MESSAGE);
        } catch (AppServiceException e) {
            httpCode = HttpCode.INTERNAL_ERROR;
            logger.error("The application with a provided invitation URL already exists.");
            result = getOperationResultExistent(RESOURCE_MESSAGE, "URL");
        } catch (IllegalArgumentException e) {
            httpCode = HttpCode.FORBIDDEN;
            logger.error("The id provided is not a correct UUID representation: {}", uuid);
            result = getOperationResultNotFound(RESOURCE_MESSAGE);
        }
        logger.debug("Returning the operation result after updating application invitation URL: {}", result);
        return Response.status(httpCode.getCode()).entity(OperationResultJsonWriter.toJson(result)).build();
    }

    private String getNameFromJson(final String body) {
        final JsonObject jsonObject = JsonReader.readAsJsonObject(body);
        return JsonReader.getStringOrNull(jsonObject, "name");
    }

    private String getDescriptionFromJson(final String body) {
        final JsonObject jsonObject = JsonReader.readAsJsonObject(body);
        return JsonReader.getStringOrNull(jsonObject, "description");
    }

    private String getURLFromJson(final String body) {
        final JsonObject jsonObject = JsonReader.readAsJsonObject(body);
        return JsonReader.getStringOrNull(jsonObject, "URL");
    }

    private String getInvitationURLFromJson(final String body) {
        final JsonObject jsonObject = JsonReader.readAsJsonObject(body);
        return JsonReader.getStringOrNull(jsonObject, "invitationURL");
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

    private boolean isUserAllowed(String email, UUID appId) {
        List<Application> apps;
        if (isLoggedAdmin(email)) {
            return true;
        }
        try {
            User user = userServices.findByEmail(email);
            apps = applicationServices.findAllAppsOfUser(user.getId());
        } catch (UserNotFoundException e) {
            return false;
        }
        return apps.stream().map(Application::getApiKey).anyMatch(o -> o.equals(appId));
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


}

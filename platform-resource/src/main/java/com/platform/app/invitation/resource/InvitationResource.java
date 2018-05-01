package com.platform.app.invitation.resource;

import com.google.gson.JsonElement;
import com.platform.app.common.exception.FieldNotValidException;
import com.platform.app.common.json.JsonUtils;
import com.platform.app.common.json.JsonWriter;
import com.platform.app.common.json.OperationResultJsonWriter;
import com.platform.app.common.model.HttpCode;
import com.platform.app.common.model.OperationResult;
import com.platform.app.common.model.PaginatedData;
import com.platform.app.common.model.ResourceMessage;
import com.platform.app.geoIP.model.GeoIP;
import com.platform.app.invitation.exception.InvitationServiceException;
import com.platform.app.invitation.model.Invitation;
import com.platform.app.invitation.services.InvitationServices;
import com.platform.app.platformUser.exception.UserNotFoundException;
import com.platform.app.program.exception.ProgramNotFoundException;
import com.platform.app.program.services.ProgramServices;
import com.platform.app.user.resource.UserJsonConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;

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
    InvitationJsonConverter invitationJsonConverter;

    @Inject
    GeoIPJsonConverter geoIPJsonConverter;

    @Inject
    UserJsonConverter userJsonConverter;

    @Context
    SecurityContext securityContext;

    @Context
    UriInfo uriInfo;


    @POST
    public Response send(String body) {
        logger.debug("Adding a new invitation with body {}", body);
        Invitation invitation = invitationJsonConverter.convertFrom(body);

        HttpCode httpCode = HttpCode.CREATED;
        OperationResult result;
        try {
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
    @Path("/program/{id}/{amount}")
    @RolesAllowed({"ADMINISTRATOR"})
    public Response sendInBatch(@PathParam("id") Long id, String body, @PathParam("amount") Integer allowedInvitations) {
        logger.debug("Sending invitations in a batch with body {}", body);
        List<String> emails = userJsonConverter.convertEmails(body);
        HttpCode httpCode = HttpCode.CREATED;
        OperationResult result;
        try {
            List<Invitation> invitations = invitationServices.sendInBatch(securityContext.getUserPrincipal().getName(),
                    id, emails, allowedInvitations);
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
    @RolesAllowed({"ADMINISTRATOR"})
    public Response findById(@PathParam("id") final Long id) {
        logger.debug("Find invitation by id: {}", id);
        Response.ResponseBuilder responseBuilder;
        try {
            Invitation invitation = invitationServices.findById(id);
            OperationResult result = OperationResult.success(invitationJsonConverter.convertToJsonElement(invitation));
            responseBuilder = Response.status(HttpCode.OK.getCode()).entity(OperationResultJsonWriter.toJson(result));
            logger.debug("Invitation found by id: {}", invitation);
        } catch (InvitationServiceException e) {
            logger.error("No invitation found for id", id);
            responseBuilder = Response.status(HttpCode.NOT_FOUND.getCode());
        }

        return responseBuilder.build();
    }

    @GET
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
    @Path("/sent")
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
    @RolesAllowed({"ADMINISTRATOR"})
    public Response findByProgram(@PathParam("id") Long id) {
        logger.debug("Finding invitations by program id: {}", id);

        List<Invitation> invitations = invitationServices.findByProgram(id);

        logger.debug("Found {} invitations", invitations.size());

        JsonElement jsonWithPagingAndEntries = JsonUtils.getJsonElementWithPagingAndEntries(
                new PaginatedData<Invitation>(invitations.size(), invitations), invitationJsonConverter);
        return Response.status(HttpCode.OK.getCode()).entity(JsonWriter.writeToString(jsonWithPagingAndEntries))
                .build();
    }

    @GET
    @Path("/all")
    @RolesAllowed({"ADMINISTRATOR"})
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
    @RolesAllowed({"ADMINISTRATOR"})
    public Response delete(@PathParam("id") Long id) {
        logger.debug("Delete invitation by id: {}", id);
        Response.ResponseBuilder responseBuilder;
        try {
            Invitation invitation = invitationServices.findById(id);
            invitationServices.delete(invitation);
            OperationResult result = OperationResult.success(invitationJsonConverter.convertToJsonElement(invitation));
            responseBuilder = Response.status(HttpCode.OK.getCode()).entity(OperationResultJsonWriter.toJson(result));
            logger.debug("Invitation deleted by id: {}", id);
        } catch (InvitationServiceException e) {
            logger.error("No invitation found for id", id);
            responseBuilder = Response.status(HttpCode.NOT_FOUND.getCode());
        }

        return responseBuilder.build();
    }

    @PUT
    @Path("/{id}/decline")
    public Response decline(@PathParam("id") Long id) {
        logger.debug("Decline invitation by id: {}", id);
        Response.ResponseBuilder responseBuilder;
        try {
            Invitation invitation = invitationServices.findById(id);
            invitationServices.decline(invitation);
            OperationResult result = OperationResult.success(invitationJsonConverter.convertToJsonElement(invitation));
            responseBuilder = Response.status(HttpCode.OK.getCode()).entity(OperationResultJsonWriter.toJson(result));
            logger.debug("Invitation decline by id: {}", id);
        } catch (InvitationServiceException e) {
            logger.error("Invitation service error: {}", e.getMessage());
            responseBuilder = Response.status(HttpCode.INTERNAL_ERROR.getCode());
        } catch (UserNotFoundException e) {
            logger.error("User not found");
            responseBuilder = Response.status(HttpCode.NOT_FOUND.getCode());
        }

        return responseBuilder.build();
    }

    @PUT
    @Path("/{id}/accept")
    public Response accept(@PathParam("id") Long id, String body) {
        logger.debug("Accept invitation by id: {} location: {}", id, body);
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
        }

        return responseBuilder.build();
    }

}

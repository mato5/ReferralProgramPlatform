package com.platform.app.program.resource;

import com.google.gson.JsonElement;
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
import com.platform.app.program.exception.ProgramNotFoundException;
import com.platform.app.program.model.Program;
import com.platform.app.program.services.ProgramServices;
import com.platform.app.user.resource.UserJsonConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;

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

    @Context
    SecurityContext securityContext;

    @Context
    UriInfo uriInfo;

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMINISTRATOR"})
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

    @GET
    @Path("/all")
    @RolesAllowed({"ADMINISTRATOR"})
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
    @Path("/admin")
    public Response findByAdmin() {
        logger.debug("Finding all programs by admin");
        List<Program> programs;
        try {
            User admin = userServices.findByEmail(securityContext.getUserPrincipal().getName());
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
}

package com.platform.app.user.resource;

//@Path("/user")
//@Produces(MediaType.APPLICATION_JSON)
//@Consumes(MediaType.APPLICATION_JSON)
public class LoggedUserResource {

    /*private Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    PlatformUserServices userServices;

    @Inject
    UserJsonConverter userJsonConverter;

    @Context
    SecurityContext securityContext;

    @Context
    UriInfo uriInfo;

    @GET
    public Response getUserPrincipal() {
        logger.debug("Fetch user principals");
        JsonObject result;
        if (securityContext.getUserPrincipal() == null) {
            result = userJsonConverter.convertPrincipals(null, null, "ROLE_NONE").getAsJsonObject();
        } else {
            Long id;
            String role;
            String email;
            try {
                email = securityContext.getUserPrincipal().getName();
                id = userServices.findByEmail(email).getId();
                if (securityContext.isUserInRole(User.Roles.ADMINISTRATOR.name())) {
                    role = "ROLE_ADMINISTRATOR";
                } else if (securityContext.isUserInRole(User.Roles.EMPLOYEE.name())) {
                    role = "ROLE_EMPLOYEE";
                } else if (securityContext.isUserInRole(User.Roles.CUSTOMER.name())) {
                    role = "ROLE_CUSTOMER";
                } else if (securityContext.isUserInRole(User.Roles.NONE.name())) {
                    role = "ROLE_NONE";
                } else {
                    role = "ROLE_NONE";
                }
            } catch (UserNotFoundException e) {
                id = null;
                email = null;
                role = "ROLE_NONE";
            }
            result = userJsonConverter.convertPrincipals(email, id, role).getAsJsonObject();
        }
        logger.debug("Fetch user principals: SUCCESS");
        return Response.status(HttpCode.OK.getCode()).entity(result).build();
    }*/

}

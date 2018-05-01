package com.platform.app.common.resource;

import com.platform.app.common.exception.UserNotAuthorizedException;
import com.platform.app.common.model.HttpCode;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class UserNotAuthorizedExceptionMapper implements ExceptionMapper<UserNotAuthorizedException> {

    @Override
    public Response toResponse(final UserNotAuthorizedException exception) {
        return Response.status(HttpCode.FORBIDDEN.getCode()).build();
    }

}
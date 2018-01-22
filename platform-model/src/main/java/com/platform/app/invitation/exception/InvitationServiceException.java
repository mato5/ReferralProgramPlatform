package com.platform.app.invitation.exception;

public class InvitationServiceException extends RuntimeException {

    public InvitationServiceException() {
        super();
    }

    public InvitationServiceException(String message, Throwable cause,
                                boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public InvitationServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvitationServiceException(String message) {
        super(message);
    }

    public InvitationServiceException(Throwable cause) {
        super(cause);
    }
}

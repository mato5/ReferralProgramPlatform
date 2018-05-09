package com.platform.app.program.exception;

import javax.ejb.ApplicationException;

@ApplicationException
public class ProgramServiceException extends RuntimeException {

    public ProgramServiceException() {
        super();
    }

    public ProgramServiceException(String message, Throwable cause,
                                   boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ProgramServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProgramServiceException(String message) {
        super(message);
    }

    public ProgramServiceException(Throwable cause) {
        super(cause);
    }
}

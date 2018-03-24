package com.platform.app.program.exception;

public class AppServiceException extends RuntimeException {

    public AppServiceException() {
        super();
    }

    public AppServiceException(String message, Throwable cause,
                                   boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public AppServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public AppServiceException(String message) {
        super(message);
    }

    public AppServiceException(Throwable cause) {
        super(cause);
    }
}


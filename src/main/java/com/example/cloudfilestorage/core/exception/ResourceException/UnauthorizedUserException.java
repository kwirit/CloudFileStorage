package com.example.cloudfilestorage.core.exception.ResourceException;

public class UnauthorizedUserException extends RuntimeException {
    public UnauthorizedUserException() {
        super();
    }

    public UnauthorizedUserException(String message) {
        super(message);
    }

    public UnauthorizedUserException(String message, Throwable cause) {
        super(message, cause);
    }

}

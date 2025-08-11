package com.example.cloudfilestorage.core.exception.AuthException;

public class ValidationAuthException extends RuntimeException{
    public ValidationAuthException() {
        super();
    }

    public ValidationAuthException(String message) {
        super(message);
    }

    public ValidationAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}

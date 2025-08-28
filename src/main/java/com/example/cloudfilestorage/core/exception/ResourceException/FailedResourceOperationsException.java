package com.example.cloudfilestorage.core.exception.ResourceException;

public class FailedResourceOperationsException extends RuntimeException {
    public FailedResourceOperationsException() {
        super();
    }

    public FailedResourceOperationsException(String message) {
        super(message);
    }

    public FailedResourceOperationsException(String message, Throwable cause) {
        super(message, cause);
    }
}

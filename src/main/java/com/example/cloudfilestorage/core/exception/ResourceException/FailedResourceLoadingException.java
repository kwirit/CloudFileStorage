package com.example.cloudfilestorage.core.exception.ResourceException;

public class FailedResourceLoadingException extends RuntimeException {
    public FailedResourceLoadingException() {
        super();
    }

    public FailedResourceLoadingException(String message) {
        super(message);
    }

    public FailedResourceLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}

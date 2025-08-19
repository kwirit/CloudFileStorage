package com.example.cloudfilestorage.core.exception.ResourceException;

public class FolderDoesNotExistException extends RuntimeException {
    public FolderDoesNotExistException() {
        super();
    }

    public FolderDoesNotExistException(String message) {
        super(message);
    }

    public FolderDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }
}

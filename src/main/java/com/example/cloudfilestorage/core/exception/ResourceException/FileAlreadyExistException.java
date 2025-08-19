package com.example.cloudfilestorage.core.exception.ResourceException;

public class FileAlreadyExistException extends RuntimeException {
    public FileAlreadyExistException() {
        super();
    }

    public FileAlreadyExistException(String message) {
        super(message);
    }

    public FileAlreadyExistException(String message, Throwable cause) {
        super(message, cause);
    }
}

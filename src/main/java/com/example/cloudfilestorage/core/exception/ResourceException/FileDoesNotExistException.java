package com.example.cloudfilestorage.core.exception.ResourceException;

public class FileDoesNotExistException extends RuntimeException {
    public FileDoesNotExistException(String message) {
        super(message);
    }
}

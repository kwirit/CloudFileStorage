package com.example.cloudfilestorage.core.exception.AuthException;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super();
    }

    public UserNotFoundException(String message) {
      super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
      super(message, cause);
    }
}

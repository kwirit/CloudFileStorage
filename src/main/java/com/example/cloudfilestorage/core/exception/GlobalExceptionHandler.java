package com.example.cloudfilestorage.core.exception;

import com.example.cloudfilestorage.core.exception.AuthException.UserAlreadyExistsException;
import com.example.cloudfilestorage.core.exception.AuthException.UserNotFoundException;
import com.example.cloudfilestorage.core.exception.AuthException.ValidationAuthException;
import com.example.cloudfilestorage.core.exception.ResourceException.UnauthorizedUserException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<String> handleUserAlreadyExistsException(UserAlreadyExistsException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
    @ExceptionHandler(ValidationAuthException.class)
    public ResponseEntity<String> handleValidationAuthException(ValidationAuthException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentialsException(BadCredentialsException ex){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }
    @ExceptionHandler(UnauthorizedUserException.class)
    public ResponseEntity<String> handleUnauthorizedUserException(UnauthorizedUserException ex){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

}

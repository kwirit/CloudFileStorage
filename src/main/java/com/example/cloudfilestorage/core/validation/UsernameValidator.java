// core/validation/UsernameValidator.java
package com.example.cloudfilestorage.core.validation;

import com.example.cloudfilestorage.core.exception.AuthException.ValidationAuthException;
import org.springframework.stereotype.Component;

@Component
public class UsernameValidator {

    private static final int MIN_LENGTH = 6;

    public void validateUsername(String username) throws ValidationAuthException {
        if (username == null) {
            throw new ValidationAuthException("Имя пользователя обязательно");
        }

        if (username.length() < MIN_LENGTH) {
            throw new ValidationAuthException(
                    String.format("Имя пользователя должно содержать минимум %d символов", MIN_LENGTH)
            );
        }

        if (Character.isDigit(username.charAt(0))) {
            throw new ValidationAuthException("Имя пользователя не должно начинаться с цифры");
        }

        if (!username.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            throw new ValidationAuthException(
                    "Имя пользователя может содержать только буквы, цифры и подчеркивание, и должно начинаться с буквы или подчеркивания"
            );
        }
    }
}
package com.example.cloudfilestorage.core.validation;

import com.example.cloudfilestorage.core.exception.AuthException.ValidationAuthException;
import org.springframework.stereotype.Component;

@Component
public class PasswordValidator {
    private static final int MIN_LENGTH = 6;
    private static final String SPECIAL_CHARS = "-()[]_!?";
    private static final String SPECIAL_CHARS_REGEX = "[-()\\[\\]_!?]"; // Экранируем спецсимволы

    public void validatePassword(String password) throws ValidationAuthException {
        if (password == null) {
            throw new ValidationAuthException("Пароль обязателен");
        }

        // Проверка минимальной длины
        if (password.length() < MIN_LENGTH) {
            throw new ValidationAuthException(
                    String.format("Пароль должен содержать минимум %d символов", MIN_LENGTH)
            );
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new ValidationAuthException("Пароль должен содержать хотя бы одну заглавную латинскую букву");
        }

        if (!password.matches(".*" + SPECIAL_CHARS_REGEX + ".*")) {
            throw new ValidationAuthException(
                    "Пароль должен содержать хотя бы один спецсимвол: " + SPECIAL_CHARS
            );
        }

        if (!password.matches(".*[0-9].*")) {
            throw new ValidationAuthException("Пароль должен содержать хотя бы одну цифру");
        }
    }
}

package com.example.cloudfilestorage.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignInRequest {

    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(min = 4, max = 10, message = "Имя пользователя должно быть от 4 до 10 символов")
    private String username;

    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 6, message = "Пароль должен быть не короче 6 символов")
    private String password;
}

package com.example.cloudfilestorage.api.dto;

import lombok.Data;

@Data
public class UserResponse {
    private String username;

    public UserResponse(String username) {
        this.username = username;
    }
}

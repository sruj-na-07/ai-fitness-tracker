package com.fitness.userservice.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data

//whatever the response we want the user to see is the fields that we include here
public class UserResponse {
    private String id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

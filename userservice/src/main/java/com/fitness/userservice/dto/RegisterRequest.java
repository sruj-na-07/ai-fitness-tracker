package com.fitness.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
//we are using validations to validate the email and the password
    @NotBlank(message="Email is required")
    @Email(message="Invalid email format")
    private String email;

    @NotBlank(message="Password is required")
    @Size(min=6 , message="Password must have atleast of 6 characters")
    private String password;

    private String firstName;
    private String lastName;

}

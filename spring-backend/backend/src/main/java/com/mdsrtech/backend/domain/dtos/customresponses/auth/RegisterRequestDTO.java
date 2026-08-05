package com.mdsrtech.backend.domain.dtos.customresponses.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterRequestDTO {
    @NotBlank(message = "Email, full name, and password are required")
    @Email(message = "Email must be valid")
    private String email;
    @NotBlank(message = "Email, full name, and password are required")
    @Size(min = 2, message = "Full name must be at least 2 characters long")
    private String fullName;
    @NotBlank(message = "Email, full name, and password are required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

}

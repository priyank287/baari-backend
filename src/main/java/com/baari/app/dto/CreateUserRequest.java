package com.baari.app.dto;

import com.baari.service.entity.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateUserRequest(
        @NotBlank String name,
        @Email @NotBlank String email,
        @NotBlank String password,
        @NotNull UserRole role,
        UUID hospitalId     // required for all roles except SUPER_ADMIN
) {}

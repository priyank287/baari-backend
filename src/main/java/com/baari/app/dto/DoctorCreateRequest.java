package com.baari.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DoctorCreateRequest(
        @NotBlank String name,
        String specialization,
        @NotNull UUID departmentId
) {}

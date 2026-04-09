package com.baari.app.dto;

import jakarta.validation.constraints.NotBlank;

public record DepartmentCreateRequest(
        @NotBlank String name
) {}

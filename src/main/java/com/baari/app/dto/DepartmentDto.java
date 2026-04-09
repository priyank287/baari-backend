package com.baari.app.dto;

import java.util.UUID;

public record DepartmentDto(
        UUID id,
        String name,
        boolean isActive
) {}

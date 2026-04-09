package com.baari.app.dto;

import java.util.UUID;

public record DoctorDto(
        UUID id,
        String name,
        String specialization,
        boolean isAvailable,
        boolean canManageQueue,
        UUID departmentId,
        String departmentName
) {}

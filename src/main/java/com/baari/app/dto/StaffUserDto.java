package com.baari.app.dto;

import com.baari.service.entity.enums.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

public record StaffUserDto(
        UUID id,
        String name,
        String email,
        UserRole role,
        boolean isActive,
        UUID hospitalId,
        UUID doctorId,
        LocalDateTime createdAt
) {}

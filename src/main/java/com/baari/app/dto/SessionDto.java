package com.baari.app.dto;

import com.baari.service.entity.enums.SessionLabel;
import com.baari.service.entity.enums.SessionStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record SessionDto(
        UUID id,
        UUID hospitalId,
        UUID doctorId,
        String doctorName,
        UUID departmentId,
        String departmentName,
        UUID createdById,
        SessionLabel label,
        LocalDate sessionDate,
        SessionStatus status,
        LocalDateTime startedAt,
        LocalDateTime closedAt
) {}

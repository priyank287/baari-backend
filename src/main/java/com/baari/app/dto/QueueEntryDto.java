package com.baari.app.dto;

import com.baari.service.entity.enums.QueueStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record QueueEntryDto(
        UUID id,
        int tokenNumber,
        String patientName,
        String mobileNumber,
        QueueStatus status,
        Integer waitTimeMinutes,
        LocalDate queueDate,
        LocalDateTime registeredAt,
        LocalDateTime calledAt,
        LocalDateTime completedAt,
        UUID sessionId,
        UUID doctorId,
        UUID departmentId
) {}

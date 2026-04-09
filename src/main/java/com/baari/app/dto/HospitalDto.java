package com.baari.app.dto;

import com.baari.service.entity.enums.PlanType;

import java.time.LocalDateTime;
import java.util.UUID;

public record HospitalDto(
        UUID id,
        String name,
        String address,
        String phone,
        String whatsappSenderId,
        PlanType planType,
        String displayToken,
        boolean displayTokenActive,
        LocalDateTime displayTokenGeneratedAt,
        boolean isActive,
        LocalDateTime createdAt
) {}

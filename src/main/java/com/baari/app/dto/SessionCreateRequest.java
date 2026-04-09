package com.baari.app.dto;

import com.baari.service.entity.enums.SessionLabel;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SessionCreateRequest(
        @NotNull UUID doctorId,
        @NotNull UUID departmentId,
        @NotNull SessionLabel label
) {}

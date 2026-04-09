package com.baari.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record QueueAddRequest(
        @NotNull UUID sessionId,
        @NotBlank String patientName,
        @NotBlank String mobileNumber
) {}

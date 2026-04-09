package com.baari.app.dto;

import jakarta.validation.constraints.NotBlank;

public record HospitalCreateRequest(
        @NotBlank String name,
        String address,
        String phone,
        String whatsappSenderId
) {}

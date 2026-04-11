package com.baari.app.dto;

import java.util.UUID;

public record LoginResponse(String token, String role, String name, UUID hospitalId, String hospitalName, String planType, UUID doctorId) {}

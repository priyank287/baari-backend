package com.baari.app.controller;

import com.baari.app.dto.AnalyticsDto;
import com.baari.app.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<AnalyticsDto.Summary> getSummary(
            @RequestParam(defaultValue = "30") int days,
            Authentication auth) {
        return ResponseEntity.ok(analyticsService.getSummary(days, auth));
    }
}

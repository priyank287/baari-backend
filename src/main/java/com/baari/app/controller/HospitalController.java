package com.baari.app.controller;

import com.baari.app.dto.HospitalCreateRequest;
import com.baari.app.dto.HospitalDto;
import com.baari.app.service.HospitalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/hospitals")
@RequiredArgsConstructor
public class HospitalController {

    private final HospitalService hospitalService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<HospitalDto> createHospital(@Valid @RequestBody HospitalCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hospitalService.createHospital(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<HospitalDto>> getAllHospitals() {
        return ResponseEntity.ok(hospitalService.getAllHospitals());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HospitalDto> getHospital(@PathVariable UUID id, Authentication auth) {
        try {
            return ResponseEntity.ok(hospitalService.getHospital(id, auth));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/display-token/regenerate")
    public ResponseEntity<HospitalDto> regenerateDisplayToken(@PathVariable UUID id, Authentication auth) {
        try {
            return ResponseEntity.ok(hospitalService.regenerateDisplayToken(id, auth));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

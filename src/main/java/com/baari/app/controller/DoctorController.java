package com.baari.app.controller;

import com.baari.app.dto.DoctorCreateRequest;
import com.baari.app.dto.DoctorDto;
import com.baari.app.service.DoctorService;
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
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping
    @PreAuthorize("hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<?> createDoctor(@Valid @RequestBody DoctorCreateRequest request,
                                          Authentication auth) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(doctorService.createDoctor(request, auth));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.getMessage());
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<DoctorDto>> getDoctors(Authentication auth) {
        try {
            return ResponseEntity.ok(doctorService.getDoctors(auth));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PatchMapping("/{id}/availability")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<DoctorDto> toggleAvailability(@PathVariable UUID id, Authentication auth) {
        try {
            return ResponseEntity.ok(doctorService.toggleAvailability(id, auth));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/queue-permission")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<DoctorDto> toggleQueuePermission(@PathVariable UUID id, Authentication auth) {
        try {
            return ResponseEntity.ok(doctorService.toggleQueuePermission(id, auth));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

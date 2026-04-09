package com.baari.app.controller;

import com.baari.app.dto.QueueAddRequest;
import com.baari.app.service.QueueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;

    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<?> addToQueue(@Valid @RequestBody QueueAddRequest request,
                                        Authentication auth) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(queueService.addToQueue(request, auth));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<?> getSessionQueue(@RequestParam UUID sessionId, Authentication auth) {
        try {
            return ResponseEntity.ok(queueService.getSessionQueue(sessionId, auth));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/call")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<?> callEntry(@PathVariable UUID id, Authentication auth) {
        try {
            return ResponseEntity.ok(queueService.callEntry(id, auth));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/done")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<?> markDone(@PathVariable UUID id, Authentication auth) {
        try {
            return ResponseEntity.ok(queueService.markDone(id, auth));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/skip")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<?> markSkipped(@PathVariable UUID id, Authentication auth) {
        try {
            return ResponseEntity.ok(queueService.markSkipped(id, auth));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/no-show")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<?> markNoShow(@PathVariable UUID id, Authentication auth) {
        try {
            return ResponseEntity.ok(queueService.markNoShow(id, auth));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Public — no auth required (permitted in SecurityConfig)
    @GetMapping("/display")
    public ResponseEntity<?> getDisplay(@RequestParam String token) {
        try {
            return ResponseEntity.ok(queueService.getDisplay(token));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid display token");
        }
    }
}

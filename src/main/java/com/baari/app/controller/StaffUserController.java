package com.baari.app.controller;

import com.baari.app.dto.StaffCreateRequest;
import com.baari.app.dto.StaffUserDto;
import com.baari.app.service.StaffUserService;
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
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffUserController {

    private final StaffUserService staffUserService;

    @PostMapping
    @PreAuthorize("hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<?> createStaff(@Valid @RequestBody StaffCreateRequest request,
                                         Authentication auth) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(staffUserService.createStaff(request, auth));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.getMessage());
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<List<StaffUserDto>> getStaff(Authentication auth) {
        return ResponseEntity.ok(staffUserService.getStaff(auth));
    }

    @GetMapping("/me")
    public ResponseEntity<StaffUserDto> getMe(Authentication auth) {
        try {
            return ResponseEntity.ok(staffUserService.getMe(auth));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<?> deactivateStaff(@PathVariable UUID id, Authentication auth) {
        try {
            return ResponseEntity.ok(staffUserService.deactivateStaff(id, auth));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

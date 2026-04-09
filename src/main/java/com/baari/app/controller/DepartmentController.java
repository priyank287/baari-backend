package com.baari.app.controller;

import com.baari.app.dto.DepartmentCreateRequest;
import com.baari.app.dto.DepartmentDto;
import com.baari.app.service.DepartmentService;
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
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    @PreAuthorize("hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<?> createDepartment(@Valid @RequestBody DepartmentCreateRequest request,
                                              Authentication auth) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.createDepartment(request, auth));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<DepartmentDto>> getActiveDepartments(Authentication auth) {
        try {
            return ResponseEntity.ok(departmentService.getActiveDepartments(auth));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<Void> deactivateDepartment(@PathVariable UUID id, Authentication auth) {
        try {
            departmentService.deactivateDepartment(id, auth);
            return ResponseEntity.noContent().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

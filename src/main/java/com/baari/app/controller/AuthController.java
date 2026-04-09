package com.baari.app.controller;

import com.baari.app.dto.CreateUserRequest;
import com.baari.app.dto.LoginRequest;
import com.baari.app.dto.LoginResponse;
import com.baari.app.repository.HospitalRepository;
import com.baari.app.repository.StaffUserRepository;
import com.baari.app.service.AuthService;
import com.baari.service.entity.StaffUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final StaffUserRepository staffUserRepository;
    private final HospitalRepository hospitalRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(authService.login(request));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody CreateUserRequest request) {
        if (staffUserRepository.findByEmail(request.email()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        StaffUser user = new StaffUser();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role());

        if (request.hospitalId() != null) {
            user.setHospital(
                    hospitalRepository.findById(request.hospitalId())
                            .orElseThrow(() -> new IllegalArgumentException("Hospital not found"))
            );
        }

        staffUserRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}

package com.baari.app.service;

import com.baari.app.dto.LoginRequest;
import com.baari.app.dto.LoginResponse;
import com.baari.app.repository.StaffUserRepository;
import com.baari.app.security.JwtService;
import com.baari.service.entity.StaffUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final StaffUserRepository staffUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        StaffUser user = staffUserRepository.findByEmail(request.email())
                .filter(StaffUser::isActive)
                .orElseThrow(() -> new org.springframework.security.authentication.BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new org.springframework.security.authentication.BadCredentialsException("Invalid credentials");
        }

        user.setLastLoginAt(LocalDateTime.now());
        staffUserRepository.save(user);

        String token = jwtService.generateToken(user);

        return new LoginResponse(
                token,
                user.getRole().name(),
                user.getName(),
                user.getHospital() != null ? user.getHospital().getId() : null
        );
    }
}

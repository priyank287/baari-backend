package com.baari.app.service;

import com.baari.app.dto.StaffCreateRequest;
import com.baari.app.dto.StaffUserDto;
import com.baari.app.repository.DoctorRepository;
import com.baari.app.repository.StaffUserRepository;
import com.baari.app.repository.SubscriptionRepository;
import com.baari.service.entity.Doctor;
import com.baari.service.entity.Hospital;
import com.baari.service.entity.StaffUser;
import com.baari.service.entity.Subscription;
import com.baari.service.entity.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StaffUserService {

    private static final Set<UserRole> CREATABLE_ROLES = Set.of(UserRole.RECEPTIONIST, UserRole.DOCTOR);

    private final StaffUserRepository staffUserRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public StaffUserDto createStaff(StaffCreateRequest request, Authentication auth) {
        if (!CREATABLE_ROLES.contains(request.role())) {
            throw new IllegalArgumentException("Role must be RECEPTIONIST or DOCTOR");
        }

        if (request.role() == UserRole.DOCTOR && request.doctorId() == null) {
            throw new IllegalArgumentException("doctorId is required when role is DOCTOR");
        }

        Hospital hospital = requireHospital(auth);

        if (staffUserRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalStateException("Email already in use");
        }

        Subscription subscription = subscriptionRepository
                .findTopByHospitalIdOrderByCreatedAtDesc(hospital.getId())
                .orElseThrow(() -> new IllegalStateException("No subscription found for hospital"));

        long currentCount = staffUserRepository.countByHospitalIdAndRoleIn(hospital.getId(), CREATABLE_ROLES);
        if (currentCount >= subscription.getMaxStaffUsers()) {
            throw new IllegalStateException(
                    "Staff user limit reached (" + subscription.getMaxStaffUsers() + "). Upgrade your plan.");
        }

        StaffUser user = new StaffUser();
        user.setHospital(hospital);
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role());

        if (request.role() == UserRole.DOCTOR) {
            Doctor doctor = doctorRepository.findById(request.doctorId())
                    .orElseThrow(() -> new NoSuchElementException("Doctor not found"));
            if (!doctor.getHospital().getId().equals(hospital.getId())) {
                throw new AccessDeniedException("Doctor does not belong to your hospital");
            }
            user.setDoctor(doctor);
        }

        return toDto(staffUserRepository.save(user));
    }

    public List<StaffUserDto> getStaff(Authentication auth) {
        Hospital hospital = requireHospital(auth);
        return staffUserRepository.findAllByHospitalId(hospital.getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public StaffUserDto deactivateStaff(UUID id, Authentication auth) {
        Hospital hospital = requireHospital(auth);

        StaffUser target = staffUserRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Staff user not found"));

        if (target.getHospital() == null || !target.getHospital().getId().equals(hospital.getId())) {
            throw new AccessDeniedException("Staff user does not belong to your hospital");
        }

        target.setActive(false);
        return toDto(staffUserRepository.save(target));
    }

    public StaffUserDto getMe(Authentication auth) {
        StaffUser user = staffUserRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        return toDto(user);
    }

    private Hospital requireHospital(Authentication auth) {
        StaffUser user = staffUserRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        if (user.getHospital() == null) {
            throw new AccessDeniedException("No hospital associated with this account");
        }
        return user.getHospital();
    }

    private StaffUserDto toDto(StaffUser u) {
        return new StaffUserDto(
                u.getId(),
                u.getName(),
                u.getEmail(),
                u.getRole(),
                u.isActive(),
                u.getHospital() != null ? u.getHospital().getId() : null,
                u.getDoctor() != null ? u.getDoctor().getId() : null,
                u.getCreatedAt()
        );
    }
}

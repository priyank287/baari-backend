package com.baari.app.service;

import com.baari.app.dto.HospitalCreateRequest;
import com.baari.app.dto.HospitalDto;
import com.baari.app.repository.HospitalRepository;
import com.baari.app.repository.StaffUserRepository;
import com.baari.app.repository.SubscriptionRepository;
import com.baari.service.entity.Hospital;
import com.baari.service.entity.StaffUser;
import com.baari.service.entity.Subscription;
import com.baari.service.entity.enums.PlanType;
import com.baari.service.entity.enums.SubscriptionStatus;
import com.baari.service.entity.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HospitalService {

    private final HospitalRepository hospitalRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final StaffUserRepository staffUserRepository;

    private static final String TOKEN_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int TOKEN_LENGTH = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Transactional
    public HospitalDto createHospital(HospitalCreateRequest request) {
        Hospital hospital = new Hospital();
        hospital.setName(request.name());
        hospital.setAddress(request.address());
        hospital.setPhone(request.phone());
        hospital.setWhatsappSenderId(request.whatsappSenderId());
        hospital.setPlanType(PlanType.BASIC);
        hospital.setDisplayToken(generateUniqueToken());
        hospital.setDisplayTokenActive(true);
        hospital.setDisplayTokenGeneratedAt(LocalDateTime.now());

        hospital = hospitalRepository.save(hospital);

        Subscription subscription = new Subscription();
        subscription.setHospital(hospital);
        subscription.setPlanType(PlanType.BASIC);
        subscription.setStatus(SubscriptionStatus.TRIAL);
        subscription.setStartDate(LocalDate.now());
        subscriptionRepository.save(subscription);

        return toDto(hospital);
    }

    public List<HospitalDto> getAllHospitals() {
        return hospitalRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public HospitalDto getHospital(UUID id, Authentication auth) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Hospital not found"));

        StaffUser user = loadUser(auth);
        if (user.getRole() != UserRole.SUPER_ADMIN) {
            if (user.getRole() != UserRole.HOSPITAL_ADMIN
                    || user.getHospital() == null
                    || !user.getHospital().getId().equals(id)) {
                throw new AccessDeniedException("Access denied");
            }
        }

        return toDto(hospital);
    }

    @Transactional
    public HospitalDto regenerateDisplayToken(UUID id, Authentication auth) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Hospital not found"));

        StaffUser user = loadUser(auth);
        if (user.getRole() != UserRole.HOSPITAL_ADMIN
                || user.getHospital() == null
                || !user.getHospital().getId().equals(id)) {
            throw new AccessDeniedException("Access denied");
        }

        hospital.setDisplayToken(generateUniqueToken());
        hospital.setDisplayTokenGeneratedAt(LocalDateTime.now());
        hospital = hospitalRepository.save(hospital);

        return toDto(hospital);
    }

    private StaffUser loadUser(Authentication auth) {
        return staffUserRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }

    private String generateUniqueToken() {
        String token;
        do {
            token = generateToken();
        } while (hospitalRepository.findByDisplayToken(token).isPresent());
        return token;
    }

    private String generateToken() {
        StringBuilder sb = new StringBuilder(TOKEN_LENGTH);
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            sb.append(TOKEN_CHARS.charAt(RANDOM.nextInt(TOKEN_CHARS.length())));
        }
        return sb.toString();
    }

    private HospitalDto toDto(Hospital h) {
        return new HospitalDto(
                h.getId(),
                h.getName(),
                h.getAddress(),
                h.getPhone(),
                h.getWhatsappSenderId(),
                h.getPlanType(),
                h.getDisplayToken(),
                h.isDisplayTokenActive(),
                h.getDisplayTokenGeneratedAt(),
                h.isActive(),
                h.getCreatedAt()
        );
    }
}

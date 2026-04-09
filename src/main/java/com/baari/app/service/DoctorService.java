package com.baari.app.service;

import com.baari.app.dto.DoctorCreateRequest;
import com.baari.app.dto.DoctorDto;
import com.baari.app.repository.DepartmentRepository;
import com.baari.app.repository.DoctorRepository;
import com.baari.app.repository.StaffUserRepository;
import com.baari.app.repository.SubscriptionRepository;
import com.baari.service.entity.Department;
import com.baari.service.entity.Doctor;
import com.baari.service.entity.Hospital;
import com.baari.service.entity.StaffUser;
import com.baari.service.entity.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final StaffUserRepository staffUserRepository;

    @Transactional
    public DoctorDto createDoctor(DoctorCreateRequest request, Authentication auth) {
        Hospital hospital = requireHospital(auth);

        Subscription subscription = subscriptionRepository
                .findTopByHospitalIdOrderByCreatedAtDesc(hospital.getId())
                .orElseThrow(() -> new IllegalStateException("No subscription found for hospital"));

        long doctorCount = doctorRepository.countByHospitalId(hospital.getId());
        if (doctorCount >= subscription.getMaxDoctors()) {
            throw new IllegalStateException(
                    "Doctor limit reached (" + subscription.getMaxDoctors() + "). Upgrade your plan.");
        }

        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new NoSuchElementException("Department not found"));

        if (!department.getHospital().getId().equals(hospital.getId())) {
            throw new AccessDeniedException("Department does not belong to your hospital");
        }

        Doctor doctor = new Doctor();
        doctor.setHospital(hospital);
        doctor.setDepartment(department);
        doctor.setName(request.name());
        doctor.setSpecialization(request.specialization());

        return toDto(doctorRepository.save(doctor));
    }

    public List<DoctorDto> getDoctors(Authentication auth) {
        Hospital hospital = requireHospital(auth);
        return doctorRepository.findAllByHospitalId(hospital.getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public DoctorDto toggleAvailability(UUID id, Authentication auth) {
        Doctor doctor = requireSameHospitalDoctor(id, auth);
        doctor.setAvailable(!doctor.isAvailable());
        return toDto(doctorRepository.save(doctor));
    }

    @Transactional
    public DoctorDto toggleQueuePermission(UUID id, Authentication auth) {
        Doctor doctor = requireSameHospitalDoctor(id, auth);
        doctor.setCanManageQueue(!doctor.isCanManageQueue());
        return toDto(doctorRepository.save(doctor));
    }

    private Doctor requireSameHospitalDoctor(UUID doctorId, Authentication auth) {
        Hospital hospital = requireHospital(auth);
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new NoSuchElementException("Doctor not found"));
        if (!doctor.getHospital().getId().equals(hospital.getId())) {
            throw new AccessDeniedException("Doctor does not belong to your hospital");
        }
        return doctor;
    }

    private Hospital requireHospital(Authentication auth) {
        StaffUser user = staffUserRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        if (user.getHospital() == null) {
            throw new AccessDeniedException("No hospital associated with this account");
        }
        return user.getHospital();
    }

    private DoctorDto toDto(Doctor d) {
        return new DoctorDto(
                d.getId(),
                d.getName(),
                d.getSpecialization(),
                d.isAvailable(),
                d.isCanManageQueue(),
                d.getDepartment().getId(),
                d.getDepartment().getName()
        );
    }
}

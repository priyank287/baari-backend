package com.baari.app.service;

import com.baari.app.dto.DepartmentCreateRequest;
import com.baari.app.dto.DepartmentDto;
import com.baari.app.repository.DepartmentRepository;
import com.baari.app.repository.StaffUserRepository;
import com.baari.app.repository.SubscriptionRepository;
import com.baari.service.entity.Department;
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
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final StaffUserRepository staffUserRepository;

    @Transactional
    public DepartmentDto createDepartment(DepartmentCreateRequest request, Authentication auth) {
        Hospital hospital = requireHospital(auth);

        Subscription subscription = subscriptionRepository
                .findTopByHospitalIdOrderByCreatedAtDesc(hospital.getId())
                .orElseThrow(() -> new IllegalStateException("No subscription found for hospital"));

        long activeCount = departmentRepository.findAllByHospitalIdAndIsActiveTrue(hospital.getId()).size();
        if (activeCount >= subscription.getMaxDepartments()) {
            throw new IllegalStateException(
                    "Department limit reached (" + subscription.getMaxDepartments() + "). Upgrade your plan.");
        }

        Department dept = new Department();
        dept.setHospital(hospital);
        dept.setName(request.name());

        return toDto(departmentRepository.save(dept));
    }

    public List<DepartmentDto> getActiveDepartments(Authentication auth) {
        Hospital hospital = requireHospital(auth);
        return departmentRepository.findAllByHospitalIdAndIsActiveTrue(hospital.getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void deactivateDepartment(UUID id, Authentication auth) {
        Hospital hospital = requireHospital(auth);

        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Department not found"));

        if (!dept.getHospital().getId().equals(hospital.getId())) {
            throw new AccessDeniedException("Department does not belong to your hospital");
        }

        dept.setActive(false);
        departmentRepository.save(dept);
    }

    private Hospital requireHospital(Authentication auth) {
        StaffUser user = staffUserRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        if (user.getHospital() == null) {
            throw new AccessDeniedException("No hospital associated with this account");
        }
        return user.getHospital();
    }

    private DepartmentDto toDto(Department d) {
        return new DepartmentDto(d.getId(), d.getName(), d.isActive());
    }
}

package com.baari.app.repository;

import com.baari.service.entity.StaffUser;
import com.baari.service.entity.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StaffUserRepository extends JpaRepository<StaffUser, UUID> {

    Optional<StaffUser> findByEmail(String email);

    List<StaffUser> findAllByHospitalId(UUID hospitalId);

    long countByHospitalIdAndRoleIn(UUID hospitalId, Collection<UserRole> roles);
}

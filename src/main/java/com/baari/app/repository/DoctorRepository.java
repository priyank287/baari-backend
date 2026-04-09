package com.baari.app.repository;

import com.baari.service.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

    List<Doctor> findAllByHospitalIdAndIsAvailableTrue(UUID hospitalId);

    List<Doctor> findAllByHospitalId(UUID hospitalId);

    long countByHospitalId(UUID hospitalId);
}

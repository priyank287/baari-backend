package com.baari.app.repository;

import com.baari.service.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HospitalRepository extends JpaRepository<Hospital, UUID> {

    Optional<Hospital> findByDisplayToken(String token);
}

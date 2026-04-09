package com.baari.app.repository;

import com.baari.service.entity.Session;
import com.baari.service.entity.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {

    List<Session> findByDoctorIdAndSessionDateAndStatus(UUID doctorId, LocalDate date, SessionStatus status);
}

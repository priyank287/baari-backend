package com.baari.app.repository;

import com.baari.service.entity.QueueEntry;
import com.baari.service.entity.enums.QueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QueueEntryRepository extends JpaRepository<QueueEntry, UUID> {

    List<QueueEntry> findAllBySessionIdAndStatusOrderByTokenNumberAsc(UUID sessionId, QueueStatus status);

    List<QueueEntry> findAllBySessionIdOrderByTokenNumberAsc(UUID sessionId);

    List<QueueEntry> findAllByHospitalIdAndQueueDateAndStatusOrderByTokenNumberAsc(
            UUID hospitalId, LocalDate queueDate, QueueStatus status);

    long countBySessionIdAndStatus(UUID sessionId, QueueStatus status);

    Optional<QueueEntry> findTopBySessionIdOrderByTokenNumberDesc(UUID sessionId);
}

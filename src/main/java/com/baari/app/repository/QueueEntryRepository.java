package com.baari.app.repository;

import com.baari.service.entity.QueueEntry;
import com.baari.service.entity.enums.QueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QueueEntryRepository extends JpaRepository<QueueEntry, UUID> {

    List<QueueEntry> findAllBySessionIdAndStatusOrderByTokenNumberAsc(UUID sessionId, QueueStatus status);

    List<QueueEntry> findAllBySessionIdOrderByTokenNumberAsc(UUID sessionId);

    List<QueueEntry> findAllByHospitalIdAndQueueDateAndStatusOrderByTokenNumberAsc(
            UUID hospitalId, LocalDate queueDate, QueueStatus status);

    List<QueueEntry> findAllBySessionIdOrderBySortKeyAsc(UUID sessionId);

    List<QueueEntry> findAllBySessionIdAndStatusOrderBySortKeyAsc(UUID sessionId, QueueStatus status);

    List<QueueEntry> findAllByHospitalIdAndQueueDateAndStatusOrderBySortKeyAsc(
            UUID hospitalId, LocalDate queueDate, QueueStatus status);

    long countBySessionIdAndStatus(UUID sessionId, QueueStatus status);

    Optional<QueueEntry> findTopBySessionIdOrderByTokenNumberDesc(UUID sessionId);

    // ── Analytics queries ──────────────────────────────────────────────────

    /** Daily totals + status breakdown for a date range */
    @Query("""
        SELECT q.queueDate,
               COUNT(q),
               SUM(CASE WHEN q.status = 'DONE'    THEN 1 ELSE 0 END),
               SUM(CASE WHEN q.status = 'NO_SHOW' THEN 1 ELSE 0 END),
               SUM(CASE WHEN q.status = 'SKIPPED' THEN 1 ELSE 0 END),
               SUM(CASE WHEN q.status = 'WAITING' OR q.status = 'CALLED' THEN 1 ELSE 0 END)
        FROM QueueEntry q
        WHERE q.hospital.id = :hospitalId
          AND q.queueDate BETWEEN :from AND :to
        GROUP BY q.queueDate
        ORDER BY q.queueDate ASC
        """)
    List<Object[]> dailyTotals(@Param("hospitalId") UUID hospitalId,
                               @Param("from") LocalDate from,
                               @Param("to") LocalDate to);

    /** Avg wait time per day */
    @Query("""
        SELECT q.queueDate, AVG(q.waitTimeMinutes)
        FROM QueueEntry q
        WHERE q.hospital.id = :hospitalId
          AND q.queueDate BETWEEN :from AND :to
          AND q.waitTimeMinutes IS NOT NULL
        GROUP BY q.queueDate
        ORDER BY q.queueDate ASC
        """)
    List<Object[]> dailyAvgWait(@Param("hospitalId") UUID hospitalId,
                                @Param("from") LocalDate from,
                                @Param("to") LocalDate to);

    /** Patient count by department for a date range */
    @Query("""
        SELECT d.name, COUNT(q)
        FROM QueueEntry q
        JOIN q.department d
        WHERE q.hospital.id = :hospitalId
          AND q.queueDate BETWEEN :from AND :to
        GROUP BY d.name
        ORDER BY COUNT(q) DESC
        """)
    List<Object[]> countByDepartment(@Param("hospitalId") UUID hospitalId,
                                     @Param("from") LocalDate from,
                                     @Param("to") LocalDate to);

    /** Avg wait time per department */
    @Query("""
        SELECT d.name, AVG(q.waitTimeMinutes)
        FROM QueueEntry q
        JOIN q.department d
        WHERE q.hospital.id = :hospitalId
          AND q.queueDate BETWEEN :from AND :to
          AND q.waitTimeMinutes IS NOT NULL
        GROUP BY d.name
        ORDER BY AVG(q.waitTimeMinutes) DESC
        """)
    List<Object[]> avgWaitByDepartment(@Param("hospitalId") UUID hospitalId,
                                       @Param("from") LocalDate from,
                                       @Param("to") LocalDate to);

    /** Avg consultation duration (minutes) for a doctor based on historical completed entries */
    @Query(value = """
        SELECT AVG(EXTRACT(EPOCH FROM (completed_at - called_at)) / 60.0)
        FROM queue_entries
        WHERE doctor_id = :doctorId
          AND completed_at IS NOT NULL
          AND called_at IS NOT NULL
        """, nativeQuery = true)
    Double avgConsultationMinutesByDoctor(@Param("doctorId") UUID doctorId);

    /** Registrations by hour of day (peak hours) */
    @Query(value = """
        SELECT EXTRACT(HOUR FROM registered_at) AS hour, COUNT(*) AS cnt
        FROM queue_entries
        WHERE hospital_id = :hospitalId
          AND queue_date BETWEEN :from AND :to
        GROUP BY hour
        ORDER BY hour ASC
        """, nativeQuery = true)
    List<Object[]> registrationsByHour(@Param("hospitalId") UUID hospitalId,
                                       @Param("from") LocalDate from,
                                       @Param("to") LocalDate to);
}

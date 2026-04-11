package com.baari.service.entity;

import com.baari.service.entity.enums.QueueStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "queue_entries",
        indexes = {
                @Index(name = "idx_queue_hospital_date",  columnList = "hospital_id, queue_date"),
                @Index(name = "idx_queue_session_status", columnList = "session_id, status"),
                @Index(name = "idx_queue_doctor_date",    columnList = "doctor_id, queue_date")
        })
public class QueueEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "patient_name", nullable = false)
    private String patientName;

    @Column(name = "mobile_number", nullable = false)
    private String mobileNumber;

    @Column(name = "token_number", nullable = false)
    private int tokenNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QueueStatus status = QueueStatus.WAITING;

    @Column(name = "wait_time_minutes")
    private Integer waitTimeMinutes;

    @CreationTimestamp
    @Column(name = "registered_at", updatable = false)
    private LocalDateTime registeredAt;

    @Column(name = "called_at")
    private LocalDateTime calledAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "queue_date", nullable = false)
    private LocalDate queueDate;

    @Column(name = "requeue_count", nullable = false)
    private int requeueCount = 0;

    @Column(name = "sort_key", nullable = false)
    private long sortKey;
}

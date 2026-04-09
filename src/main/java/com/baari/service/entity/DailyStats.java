package com.baari.service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "daily_stats",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"hospital_id", "doctor_id", "stat_date"})
        })
public class DailyStats {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "total_patients")
    private int totalPatients = 0;

    private int completed = 0;
    private int skipped = 0;

    @Column(name = "no_shows")
    private int noShows = 0;

    @Column(name = "avg_wait_minutes")
    private int avgWaitMinutes = 0;

    @Column(name = "peak_hour")
    private int peakHour = 0;          // 0-23 hour of day
}

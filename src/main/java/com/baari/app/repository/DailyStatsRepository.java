package com.baari.app.repository;

import com.baari.service.entity.DailyStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DailyStatsRepository extends JpaRepository<DailyStats, UUID> {
}

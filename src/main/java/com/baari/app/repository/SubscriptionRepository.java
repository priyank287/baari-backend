package com.baari.app.repository;

import com.baari.service.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    Optional<Subscription> findTopByHospitalIdOrderByCreatedAtDesc(UUID hospitalId);
}

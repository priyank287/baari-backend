package com.baari.service.entity;

import com.baari.service.entity.enums.PlanType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "hospitals")
public class Hospital {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String address;
    private String phone;

    @Column(name = "whatsapp_sender_id")
    private String whatsappSenderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false)
    private PlanType planType = PlanType.BASIC;

    @Column(name = "display_token", unique = true)
    private String displayToken;

    @Column(name = "display_token_active")
    private boolean displayTokenActive = true;

    @Column(name = "display_token_generated_at")
    private LocalDateTime displayTokenGeneratedAt;

    @Column(name = "is_active")
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

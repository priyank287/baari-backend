package com.baari.app.repository;

import com.baari.service.entity.NotificationLog;
import com.baari.service.entity.QueueEntry;
import com.baari.service.entity.enums.MessageType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {

    boolean existsByQueueEntryAndMessageTypeAndSentAtAfter(
            QueueEntry queueEntry, MessageType messageType, LocalDateTime after);
}

package com.baari.app.service;

import com.baari.app.notification.NotificationChannel;
import com.baari.app.repository.NotificationLogRepository;
import com.baari.app.repository.QueueEntryRepository;
import com.baari.service.entity.NotificationLog;
import com.baari.service.entity.QueueEntry;
import com.baari.service.entity.enums.DeliveryStatus;
import com.baari.service.entity.enums.MessageType;
import com.baari.service.entity.enums.QueueStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    private final NotificationChannel notificationChannel;
    private final NotificationLogRepository notificationLogRepository;
    private final QueueEntryRepository queueEntryRepository;

    public void sendRegistrationSms(QueueEntry entry) {
        long position = queueEntryRepository.countBySessionIdAndStatus(
                entry.getSession().getId(), QueueStatus.WAITING);

        Double avgConsultation = queueEntryRepository.avgConsultationMinutesByDoctor(entry.getDoctor().getId());
        long estWait = Math.round((avgConsultation != null ? avgConsultation : 10.0) * position);

        String message = String.format(
                "Token #%d confirmed!\n\nDoctor: Dr. %s\nPosition: #%d in queue\nEst. wait: ~%d mins\n\n- Baari",
                entry.getTokenNumber(),
                entry.getDoctor().getName(),
                position,
                estWait);

        send(entry, message, MessageType.REGISTERED);
    }

    public void sendYouAreNextSms(QueueEntry entry) {
        boolean alreadySent = notificationLogRepository.existsByQueueEntryAndMessageTypeAndSentAtAfter(
                entry, MessageType.YOU_ARE_NEXT, LocalDateTime.now().minusSeconds(60));
        if (alreadySent) {
            log.info("YOU_ARE_NEXT notification skipped for entry {} — already sent within 60s", entry.getId());
            return;
        }

        String message = String.format(
                "Agli baari aapki!\n\nYou're next. Please proceed to\nDr. %s's cabin now.\n\n- Baari",
                entry.getDoctor().getName());

        send(entry, message, MessageType.YOU_ARE_NEXT);
    }

    public void sendReminderSms(QueueEntry entry) {
        String message = String.format(
                "Reminder: Aapki baari aa gayi!\n\nDr. %s is waiting for you.\nPlease proceed to the cabin now.\n\n- Baari",
                entry.getDoctor().getName());

        send(entry, message, MessageType.REMINDER);
    }

    public void sendRequeueSms(QueueEntry entry) {
        String message = String.format(
                "Token #%d update:\n\nYou've been rescheduled in the queue.\nDr. %s will call you again shortly.\n\n- Baari",
                entry.getTokenNumber(),
                entry.getDoctor().getName());

        send(entry, message, MessageType.POSITION_UPDATE);
    }

    public void sendNoShowSms(QueueEntry entry) {
        String message = String.format(
                "Token #%d has expired.\n\nPlease visit the reception to\nre-register.\n\n- Baari",
                entry.getTokenNumber());

        send(entry, message, MessageType.NO_SHOW_ALERT);
    }

    private void send(QueueEntry entry, String message, MessageType messageType) {
        boolean delivered = notificationChannel.send(entry.getMobileNumber(), message, messageType);
        DeliveryStatus status = delivered ? DeliveryStatus.SENT : DeliveryStatus.FAILED;
        saveLog(entry, message, messageType, status);
    }

    private void saveLog(QueueEntry entry, String message, MessageType messageType, DeliveryStatus status) {
        try {
            NotificationLog record = new NotificationLog();
            record.setQueueEntry(entry);
            record.setMessageType(messageType);
            record.setWhatsappMessage(message);
            record.setDeliveryStatus(status);
            record.setSentAt(LocalDateTime.now());
            notificationLogRepository.save(record);
        } catch (Exception e) {
            log.error("Failed to save notification log for entry {}: {}", entry.getId(), e.getMessage());
        }
    }
}

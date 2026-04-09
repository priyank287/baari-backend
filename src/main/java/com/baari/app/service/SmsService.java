package com.baari.app.service;

import com.baari.app.repository.NotificationLogRepository;
import com.baari.app.repository.QueueEntryRepository;
import com.baari.service.entity.NotificationLog;
import com.baari.service.entity.QueueEntry;
import com.baari.service.entity.enums.DeliveryStatus;
import com.baari.service.entity.enums.MessageType;
import com.baari.service.entity.enums.QueueStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    private static final String FAST2SMS_URL = "https://www.fast2sms.com/dev/bulkV2";

    private final RestTemplate restTemplate;
    private final NotificationLogRepository notificationLogRepository;
    private final QueueEntryRepository queueEntryRepository;

    @Value("${fast2sms.api-key}")
    private String apiKey;

    public void sendRegistrationSms(QueueEntry entry) {
        long position = queueEntryRepository.countBySessionIdAndStatus(
                entry.getSession().getId(), QueueStatus.WAITING);

        String message = String.format(
                "Token #%d confirmed for Dr.%s. You are #%d in queue. ~Baari",
                entry.getTokenNumber(),
                entry.getDoctor().getName(),
                position);

        send(entry, message, MessageType.REGISTERED);
    }

    public void sendYouAreNextSms(QueueEntry entry) {
        String message = String.format(
                "You are next! Please proceed to Dr.%s cabin. ~Baari",
                entry.getDoctor().getName());

        send(entry, message, MessageType.YOU_ARE_NEXT);
    }

    public void sendNoShowSms(QueueEntry entry) {
        String message = String.format(
                "Token #%d expired. Visit reception to re-register. ~Baari",
                entry.getTokenNumber());

        send(entry, message, MessageType.NO_SHOW_ALERT);
    }

    private void send(QueueEntry entry, String message, MessageType messageType) {
        DeliveryStatus status = DeliveryStatus.FAILED;

        try {
            String url = UriComponentsBuilder.fromUri(java.net.URI.create(FAST2SMS_URL))
                    .queryParam("route", "q")
                    .queryParam("message", message)
                    .queryParam("language", "english")
                    .queryParam("numbers", entry.getMobileNumber())
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("authorization", apiKey);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                status = DeliveryStatus.SENT;
                log.info("SMS sent [{}] to {} for entry {}", messageType, entry.getMobileNumber(), entry.getId());
            } else {
                log.warn("SMS failed [{}] — HTTP {}", messageType, response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("SMS error [{}] for entry {}: {}", messageType, entry.getId(), e.getMessage());
        }

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

package com.baari.app.service;

import com.baari.app.dto.QueueEntryDto;
import com.baari.app.repository.QueueEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QueueWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final QueueEntryRepository queueEntryRepository;

    public void broadcastQueueUpdate(UUID sessionId) {
        List<QueueEntryDto> queue = queueEntryRepository
                .findAllBySessionIdOrderByTokenNumberAsc(sessionId)
                .stream()
                .map(e -> new QueueEntryDto(
                        e.getId(),
                        e.getTokenNumber(),
                        e.getPatientName(),
                        e.getMobileNumber(),
                        e.getStatus(),
                        e.getWaitTimeMinutes(),
                        e.getQueueDate(),
                        e.getRegisteredAt(),
                        e.getCalledAt(),
                        e.getCompletedAt(),
                        e.getSession().getId(),
                        e.getDoctor().getId(),
                        e.getDepartment().getId()
                ))
                .toList();

        messagingTemplate.convertAndSend("/topic/queue/" + sessionId, queue);
    }
}

package com.baari.app.service;

import com.baari.app.dto.QueueAddRequest;
import com.baari.app.dto.QueueDisplayDto;
import com.baari.app.dto.QueueEntryDto;
import com.baari.app.repository.HospitalRepository;
import com.baari.app.repository.QueueEntryRepository;
import com.baari.app.repository.SessionRepository;
import com.baari.app.repository.StaffUserRepository;
import com.baari.service.entity.Hospital;
import com.baari.service.entity.QueueEntry;
import com.baari.service.entity.Session;
import com.baari.service.entity.StaffUser;
import com.baari.service.entity.enums.QueueStatus;
import com.baari.service.entity.enums.SessionStatus;
import com.baari.service.entity.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final QueueEntryRepository queueEntryRepository;
    private final SessionRepository sessionRepository;
    private final StaffUserRepository staffUserRepository;
    private final HospitalRepository hospitalRepository;
    private final QueueWebSocketService queueWebSocketService;
    private final SmsService smsService;

    @Transactional
    public QueueEntryDto addToQueue(QueueAddRequest request, Authentication auth) {
        StaffUser caller = loadUser(auth);
        Hospital hospital = requireHospital(caller);

        Session session = sessionRepository.findById(request.sessionId())
                .orElseThrow(() -> new NoSuchElementException("Session not found"));

        if (!session.getHospital().getId().equals(hospital.getId())) {
            throw new AccessDeniedException("Session does not belong to your hospital");
        }
        if (session.getStatus() != SessionStatus.OPEN) {
            throw new IllegalStateException("Session is not open");
        }

        int nextToken = queueEntryRepository
                .findTopBySessionIdOrderByTokenNumberDesc(session.getId())
                .map(e -> e.getTokenNumber() + 1)
                .orElse(1);

        QueueEntry entry = new QueueEntry();
        entry.setHospital(hospital);
        entry.setSession(session);
        entry.setDoctor(session.getDoctor());
        entry.setDepartment(session.getDepartment());
        entry.setPatientName(request.patientName());
        entry.setMobileNumber(request.mobileNumber());
        entry.setTokenNumber(nextToken);
        entry.setSortKey((long) nextToken * 1000);
        entry.setQueueDate(LocalDate.now());

        QueueEntry saved = queueEntryRepository.save(entry);
        queueWebSocketService.broadcastQueueUpdate(session.getId());
        smsService.sendRegistrationSms(saved);
        return toDto(saved);
    }

    public List<QueueEntryDto> getSessionQueue(UUID sessionId, Authentication auth) {
        Hospital hospital = requireHospital(loadUser(auth));

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NoSuchElementException("Session not found"));

        if (!session.getHospital().getId().equals(hospital.getId())) {
            throw new AccessDeniedException("Session does not belong to your hospital");
        }

        return queueEntryRepository.findAllBySessionIdOrderBySortKeyAsc(sessionId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public QueueEntryDto callEntry(UUID id, Authentication auth) {
        StaffUser caller = loadUser(auth);
        QueueEntry entry = requireSameHospitalEntry(id, caller);

        if (caller.getRole() == UserRole.DOCTOR) {
            if (caller.getDoctor() == null || !caller.getDoctor().isCanManageQueue()) {
                throw new AccessDeniedException("Doctor does not have queue management permission");
            }
        }

        if (entry.getStatus() != QueueStatus.WAITING && entry.getStatus() != QueueStatus.CALLED) {
            throw new IllegalStateException("Only WAITING or CALLED entries can be called");
        }

        // Re-call: already CALLED — just broadcast again for TV display, no state change needed
        if (entry.getStatus() == QueueStatus.CALLED) {
            queueWebSocketService.broadcastQueueUpdate(entry.getSession().getId());
            return toDto(entry);
        }

        // Capture position-1 check before status changes
        List<QueueEntry> waitingBefore = queueEntryRepository
                .findAllBySessionIdAndStatusOrderBySortKeyAsc(entry.getSession().getId(), QueueStatus.WAITING);
        boolean wasFirst = !waitingBefore.isEmpty() && waitingBefore.get(0).getId().equals(id);

        LocalDateTime now = LocalDateTime.now();
        entry.setStatus(QueueStatus.CALLED);
        entry.setCalledAt(now);
        if (entry.getRegisteredAt() != null) {
            entry.setWaitTimeMinutes((int) ChronoUnit.MINUTES.between(entry.getRegisteredAt(), now));
        }

        QueueEntry saved = queueEntryRepository.save(entry);
        queueWebSocketService.broadcastQueueUpdate(saved.getSession().getId());

        // Notify the patient who is now position 1
        if (wasFirst) {
            List<QueueEntry> waitingAfter = queueEntryRepository
                    .findAllBySessionIdAndStatusOrderBySortKeyAsc(saved.getSession().getId(), QueueStatus.WAITING);
            if (!waitingAfter.isEmpty()) {
                smsService.sendYouAreNextSms(waitingAfter.get(0));
            }
        }

        return toDto(saved);
    }

    @Transactional
    public QueueEntryDto markDone(UUID id, Authentication auth) {
        StaffUser caller = loadUser(auth);
        QueueEntry entry = requireSameHospitalEntry(id, caller);

        if (caller.getRole() == UserRole.DOCTOR) {
            if (caller.getDoctor() == null || !caller.getDoctor().isCanManageQueue()) {
                throw new AccessDeniedException("Doctor does not have queue management permission");
            }
        }

        entry.setStatus(QueueStatus.DONE);
        entry.setCompletedAt(LocalDateTime.now());

        QueueEntryDto dto = toDto(queueEntryRepository.save(entry));
        queueWebSocketService.broadcastQueueUpdate(entry.getSession().getId());
        return dto;
    }

    @Transactional
    public QueueEntryDto markSkipped(UUID id, Authentication auth) {
        StaffUser caller = loadUser(auth);
        QueueEntry entry = requireSameHospitalEntry(id, caller);
        entry.setStatus(QueueStatus.SKIPPED);
        QueueEntryDto dto = toDto(queueEntryRepository.save(entry));
        queueWebSocketService.broadcastQueueUpdate(entry.getSession().getId());
        return dto;
    }

    @Transactional
    public QueueEntryDto markNoShow(UUID id, Authentication auth) {
        StaffUser caller = loadUser(auth);
        QueueEntry entry = requireSameHospitalEntry(id, caller);
        entry.setStatus(QueueStatus.NO_SHOW);
        QueueEntry saved = queueEntryRepository.save(entry);
        queueWebSocketService.broadcastQueueUpdate(saved.getSession().getId());
        smsService.sendNoShowSms(saved);
        return toDto(saved);
    }

    @Transactional
    public QueueEntryDto requeueEntry(UUID id, Authentication auth) {
        StaffUser caller = loadUser(auth);
        QueueEntry entry = requireSameHospitalEntry(id, caller);

        if (entry.getStatus() != QueueStatus.CALLED) {
            throw new IllegalStateException("Only CALLED entries can be requeued");
        }

        List<QueueEntry> waiting = queueEntryRepository
                .findAllBySessionIdAndStatusOrderBySortKeyAsc(entry.getSession().getId(), QueueStatus.WAITING);

        long newSortKey;
        if (entry.getRequeueCount() == 0 && waiting.size() >= 3) {
            // Insert between 2nd and 3rd waiting (position 3)
            long k1 = waiting.get(1).getSortKey();
            long k2 = waiting.get(2).getSortKey();
            newSortKey = (k1 + k2) / 2;
            if (newSortKey <= k1) newSortKey = k1 + 1;
        } else {
            // Place at end
            newSortKey = waiting.isEmpty()
                    ? (long) entry.getTokenNumber() * 1000
                    : waiting.get(waiting.size() - 1).getSortKey() + 1000L;
        }

        entry.setStatus(QueueStatus.WAITING);
        entry.setCalledAt(null);
        entry.setWaitTimeMinutes(null);
        entry.setRequeueCount(entry.getRequeueCount() + 1);
        entry.setSortKey(newSortKey);

        QueueEntry saved = queueEntryRepository.save(entry);
        queueWebSocketService.broadcastQueueUpdate(saved.getSession().getId());
        smsService.sendRequeueSms(saved);
        return toDto(saved);
    }

    @Transactional
    public QueueEntryDto sendReminder(UUID id, Authentication auth) {
        StaffUser caller = loadUser(auth);
        QueueEntry entry = requireSameHospitalEntry(id, caller);

        if (entry.getStatus() != QueueStatus.CALLED) {
            throw new IllegalStateException("Only CALLED entries can receive a reminder");
        }

        smsService.sendReminderSms(entry);
        return toDto(entry);
    }

    public QueueDisplayDto getDisplay(String displayToken) {
        Hospital hospital = hospitalRepository.findByDisplayToken(displayToken)
                .orElseThrow(() -> new NoSuchElementException("Invalid display token"));

        if (!hospital.isDisplayTokenActive()) {
            throw new AccessDeniedException("Display token is inactive");
        }

        List<QueueDisplayDto.SessionRow> rows = sessionRepository
                .findByHospitalIdAndSessionDateAndStatus(hospital.getId(), LocalDate.now(), SessionStatus.OPEN)
                .stream()
                .map(session -> {
                    List<QueueEntry> called = queueEntryRepository
                            .findAllBySessionIdAndStatusOrderBySortKeyAsc(session.getId(), QueueStatus.CALLED);
                    List<QueueEntry> waiting = queueEntryRepository
                            .findAllBySessionIdAndStatusOrderBySortKeyAsc(session.getId(), QueueStatus.WAITING);

                    QueueDisplayDto.DisplayEntry inConsultation = called.isEmpty() ? null
                            : new QueueDisplayDto.DisplayEntry(called.get(0).getTokenNumber(), called.get(0).getPatientName());

                    List<QueueDisplayDto.DisplayEntry> upNext = waiting.stream()
                            .limit(2)
                            .map(e -> new QueueDisplayDto.DisplayEntry(e.getTokenNumber(), e.getPatientName()))
                            .toList();

                    return new QueueDisplayDto.SessionRow(
                            session.getDoctor().getName(),
                            session.getDepartment().getName(),
                            inConsultation,
                            upNext
                    );
                })
                .toList();

        return new QueueDisplayDto(hospital.getName(), rows);
    }

    // --- helpers ---

    private QueueEntry requireSameHospitalEntry(UUID entryId, StaffUser caller) {
        Hospital hospital = requireHospital(caller);
        QueueEntry entry = queueEntryRepository.findById(entryId)
                .orElseThrow(() -> new NoSuchElementException("Queue entry not found"));
        if (!entry.getHospital().getId().equals(hospital.getId())) {
            throw new AccessDeniedException("Entry does not belong to your hospital");
        }
        return entry;
    }

    private StaffUser loadUser(Authentication auth) {
        return staffUserRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }

    private Hospital requireHospital(StaffUser user) {
        if (user.getHospital() == null) {
            throw new AccessDeniedException("No hospital associated with this account");
        }
        return user.getHospital();
    }

    private QueueEntryDto toDto(QueueEntry e) {
        return new QueueEntryDto(
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
                e.getDepartment().getId(),
                e.getRequeueCount()
        );
    }
}

package com.baari.app.service;

import com.baari.app.dto.SessionCreateRequest;
import com.baari.app.dto.SessionDto;
import com.baari.app.repository.DepartmentRepository;
import com.baari.app.repository.DoctorRepository;
import com.baari.app.repository.SessionRepository;
import com.baari.app.repository.StaffUserRepository;
import com.baari.service.entity.Department;
import com.baari.service.entity.Doctor;
import com.baari.service.entity.Hospital;
import com.baari.service.entity.Session;
import com.baari.service.entity.StaffUser;
import com.baari.service.entity.enums.SessionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final StaffUserRepository staffUserRepository;

    @Transactional
    public SessionDto openSession(SessionCreateRequest request, Authentication auth) {
        StaffUser caller = loadUser(auth);
        Hospital hospital = requireHospital(caller);

        Doctor doctor = doctorRepository.findById(request.doctorId())
                .orElseThrow(() -> new NoSuchElementException("Doctor not found"));
        if (!doctor.getHospital().getId().equals(hospital.getId())) {
            throw new AccessDeniedException("Doctor does not belong to your hospital");
        }

        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new NoSuchElementException("Department not found"));
        if (!department.getHospital().getId().equals(hospital.getId())) {
            throw new AccessDeniedException("Department does not belong to your hospital");
        }

        List<Session> openToday = sessionRepository.findByDoctorIdAndSessionDateAndStatus(
                request.doctorId(), LocalDate.now(), SessionStatus.OPEN);
        if (!openToday.isEmpty()) {
            throw new IllegalStateException("An open session already exists for this doctor today");
        }

        Session session = new Session();
        session.setHospital(hospital);
        session.setDoctor(doctor);
        session.setDepartment(department);
        session.setCreatedBy(caller);
        session.setLabel(request.label());
        session.setSessionDate(LocalDate.now());
        session.setStartedAt(LocalDateTime.now());

        return toDto(sessionRepository.save(session));
    }

    public Optional<SessionDto> getActiveSession(UUID doctorId, Authentication auth) {
        Hospital hospital = requireHospital(loadUser(auth));

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new NoSuchElementException("Doctor not found"));
        if (!doctor.getHospital().getId().equals(hospital.getId())) {
            throw new AccessDeniedException("Doctor does not belong to your hospital");
        }

        return sessionRepository
                .findByDoctorIdAndSessionDateAndStatus(doctorId, LocalDate.now(), SessionStatus.OPEN)
                .stream()
                .findFirst()
                .map(this::toDto);
    }

    @Transactional
    public SessionDto closeSession(UUID id, Authentication auth) {
        Hospital hospital = requireHospital(loadUser(auth));

        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Session not found"));

        if (!session.getHospital().getId().equals(hospital.getId())) {
            throw new AccessDeniedException("Session does not belong to your hospital");
        }

        if (session.getStatus() == SessionStatus.CLOSED) {
            throw new IllegalStateException("Session is already closed");
        }

        session.setStatus(SessionStatus.CLOSED);
        session.setClosedAt(LocalDateTime.now());

        return toDto(sessionRepository.save(session));
    }

    public List<SessionDto> getOpenSessions(Authentication auth) {
        Hospital hospital = requireHospital(loadUser(auth));
        return sessionRepository
                .findByHospitalIdAndSessionDateAndStatus(hospital.getId(), LocalDate.now(), SessionStatus.OPEN)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<SessionDto> listSessions(UUID doctorId, LocalDate date, Authentication auth) {
        Hospital hospital = requireHospital(loadUser(auth));

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new NoSuchElementException("Doctor not found"));
        if (!doctor.getHospital().getId().equals(hospital.getId())) {
            throw new AccessDeniedException("Doctor does not belong to your hospital");
        }

        return sessionRepository.findByDoctorIdAndSessionDate(doctorId, date)
                .stream()
                .map(this::toDto)
                .toList();
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

    private SessionDto toDto(Session s) {
        return new SessionDto(
                s.getId(),
                s.getHospital().getId(),
                s.getDoctor().getId(),
                s.getDoctor().getName(),
                s.getDepartment().getId(),
                s.getDepartment().getName(),
                s.getCreatedBy().getId(),
                s.getLabel(),
                s.getSessionDate(),
                s.getStatus(),
                s.getStartedAt(),
                s.getClosedAt()
        );
    }
}

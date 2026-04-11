package com.baari.app.service;

import com.baari.app.dto.AnalyticsDto;
import com.baari.app.repository.QueueEntryRepository;
import com.baari.app.repository.StaffUserRepository;
import com.baari.service.entity.Hospital;
import com.baari.service.entity.StaffUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final QueueEntryRepository queueEntryRepository;
    private final StaffUserRepository  staffUserRepository;

    public AnalyticsDto.Summary getSummary(int days, Authentication auth) {
        Hospital hospital = requireHospital(auth);
        LocalDate to   = LocalDate.now();
        LocalDate from = to.minusDays(days - 1);
        UUID hid = hospital.getId();

        List<AnalyticsDto.DailyVolume> dailyVolume = queueEntryRepository
                .dailyTotals(hid, from, to)
                .stream()
                .map(r -> new AnalyticsDto.DailyVolume(
                        r[0].toString(),
                        toLong(r[1]),
                        toLong(r[2]),
                        toLong(r[3]),
                        toLong(r[4]),
                        toLong(r[5])
                ))
                .toList();

        List<AnalyticsDto.DailyWait> dailyWait = queueEntryRepository
                .dailyAvgWait(hid, from, to)
                .stream()
                .map(r -> new AnalyticsDto.DailyWait(
                        r[0].toString(),
                        toDouble(r[1])
                ))
                .toList();

        List<AnalyticsDto.DepartmentVolume> departmentVolume = queueEntryRepository
                .countByDepartment(hid, from, to)
                .stream()
                .map(r -> new AnalyticsDto.DepartmentVolume(
                        r[0].toString(),
                        toLong(r[1])
                ))
                .toList();

        List<AnalyticsDto.HourlyVolume> hourlyVolume = queueEntryRepository
                .registrationsByHour(hid, from, to)
                .stream()
                .map(r -> new AnalyticsDto.HourlyVolume(
                        ((Number) r[0]).intValue(),
                        toLong(r[1])
                ))
                .toList();

        List<AnalyticsDto.DepartmentWait> departmentWait = queueEntryRepository
                .avgWaitByDepartment(hid, from, to)
                .stream()
                .map(r -> new AnalyticsDto.DepartmentWait(
                        r[0].toString(),
                        toDouble(r[1])
                ))
                .toList();

        return new AnalyticsDto.Summary(dailyVolume, dailyWait, departmentVolume, hourlyVolume, departmentWait);
    }

    private Hospital requireHospital(Authentication auth) {
        StaffUser user = staffUserRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        if (user.getHospital() == null) {
            throw new AccessDeniedException("No hospital associated with this account");
        }
        return user.getHospital();
    }

    private long toLong(Object o) {
        if (o == null) return 0L;
        if (o instanceof Number n) return n.longValue();
        return Long.parseLong(o.toString());
    }

    private double toDouble(Object o) {
        if (o == null) return 0.0;
        if (o instanceof BigDecimal bd) return bd.doubleValue();
        if (o instanceof Number n) return n.doubleValue();
        return Double.parseDouble(o.toString());
    }
}

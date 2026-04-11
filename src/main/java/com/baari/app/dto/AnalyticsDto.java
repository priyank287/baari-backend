package com.baari.app.dto;

import java.util.List;

public class AnalyticsDto {

    public record DailyVolume(
            String date,
            long total,
            long done,
            long noShow,
            long skipped,
            long pending
    ) {}

    public record DailyWait(
            String date,
            double avgWaitMinutes
    ) {}

    public record DepartmentVolume(
            String department,
            long count
    ) {}

    public record HourlyVolume(
            int hour,
            long count
    ) {}

    public record DepartmentWait(
            String department,
            double avgWaitMinutes
    ) {}

    public record Summary(
            List<DailyVolume>      dailyVolume,
            List<DailyWait>        dailyWait,
            List<DepartmentVolume> departmentVolume,
            List<HourlyVolume>     hourlyVolume,
            List<DepartmentWait>   departmentWait
    ) {}
}

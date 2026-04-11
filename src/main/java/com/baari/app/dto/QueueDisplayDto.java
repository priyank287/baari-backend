package com.baari.app.dto;

import java.util.List;

public record QueueDisplayDto(
        String hospitalName,
        List<SessionRow> sessions
) {
    public record SessionRow(
            String doctorName,
            String departmentName,
            DisplayEntry inConsultation,
            List<DisplayEntry> upNext
    ) {}

    public record DisplayEntry(int tokenNumber, String patientName) {}
}

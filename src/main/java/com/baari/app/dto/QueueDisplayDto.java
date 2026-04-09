package com.baari.app.dto;

import java.util.List;

public record QueueDisplayDto(
        List<DisplayEntry> called,
        List<DisplayEntry> next
) {
    public record DisplayEntry(int tokenNumber, String patientName) {}
}

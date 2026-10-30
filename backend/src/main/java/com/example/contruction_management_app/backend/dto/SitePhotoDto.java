package com.example.contruction_management_app.backend.dto;

import com.example.contruction_management_app.backend.domain.SitePhoto;

import java.time.Instant;

public record SitePhotoDto(
    String id,
    String projectId,
    String reportId,
    String taskId,
    String defectId,
    String photoUrl,
    String caption,
    String locationTag,
    Instant capturedAt,
    String uploadedBy
) {
    public static SitePhotoDto fromEntity(SitePhoto p) {
        return new SitePhotoDto(
            p.getId(),
            p.getProject().getId(),
            p.getReportId(),
            p.getTaskId(),
            p.getDefectId(),
            p.getPhotoUrl(),
            p.getCaption(),
            p.getLocationTag(),
            p.getCapturedAt(),
            p.getUploadedBy()
        );
    }
}

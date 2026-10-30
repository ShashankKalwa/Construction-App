package com.example.contruction_management_app.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "site_photos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SitePhoto {

    @Id
    @Column(length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "company_id", nullable = false, length = 50)
    private String companyId;

    @Column(name = "report_id", length = 50)
    private String reportId;

    @Column(name = "task_id", length = 50)
    private String taskId;

    @Column(name = "defect_id", length = 50)
    private String defectId;

    @Column(name = "photo_url", nullable = false, length = 500)
    private String photoUrl;

    @Column(length = 255)
    private String caption;

    @Column(name = "location_tag", length = 150)
    private String locationTag;

    @CreationTimestamp
    @Column(name = "captured_at", nullable = false, updatable = false)
    private Instant capturedAt;

    @Column(name = "uploaded_by", nullable = false, length = 50)
    private String uploadedBy;
}

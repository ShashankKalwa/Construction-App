package com.example.contruction_management_app.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "site_defects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteDefect {

    @Id
    @Column(length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "company_id", nullable = false, length = 50)
    private String companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspection_id")
    private SiteInspection inspection;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(length = 200)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private IssueSeverity severity = IssueSeverity.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private DefectStatus status = DefectStatus.OPEN;

    @Column(name = "assigned_to", length = 50)
    private String assignedTo;

    @Column(name = "deadline")
    private LocalDate deadline;

    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "re_inspected_by", length = 50)
    private String reInspectedBy;

    @Column(name = "re_inspected_at")
    private Instant reInspectedAt;

    @Column(name = "re_inspection_notes", columnDefinition = "TEXT")
    private String reInspectionNotes;
}

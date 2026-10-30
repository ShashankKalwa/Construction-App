package com.example.contruction_management_app.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "site_inspections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteInspection {

    @Id
    @Column(length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "company_id", nullable = false, length = 50)
    private String companyId;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "inspection_type", nullable = false, length = 50)
    private InspectionType inspectionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private InspectionStatus status = InspectionStatus.PASSED;

    @Column(name = "checklist_data", nullable = false, columnDefinition = "TEXT")
    private String checklistData;

    @Column(name = "inspector_id", nullable = false, length = 50)
    private String inspectorId;

    @Column(name = "inspection_date", nullable = false)
    private LocalDate inspectionDate;

    @Column(length = 200)
    private String location;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}

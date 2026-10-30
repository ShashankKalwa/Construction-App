package com.example.contruction_management_app.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "site_work_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkProgressRecord {

    @Id
    @Column(length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "company_id", nullable = false, length = 50)
    private String companyId;

    @Column(name = "task_id", length = 50)
    private String taskId;

    @Column(name = "report_id", length = 50)
    private String reportId;

    @Column(name = "item_name", nullable = false, length = 200)
    private String itemName;

    @Column(nullable = false, length = 30)
    private String unit;

    @Column(name = "planned_quantity", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal plannedQuantity = BigDecimal.ZERO;

    @Column(name = "reported_quantity", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal reportedQuantity = BigDecimal.ZERO;

    @Column(name = "verified_quantity", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal verifiedQuantity = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private WorkProgressStatus status = WorkProgressStatus.REPORTED;

    @Column(name = "reported_by", nullable = false, length = 50)
    private String reportedBy;

    @Column(name = "verified_by", length = 50)
    private String verifiedBy;

    @Column(name = "verification_notes", columnDefinition = "TEXT")
    private String verificationNotes;

    @CreationTimestamp
    @Column(name = "recorded_at", nullable = false, updatable = false)
    private Instant recordedAt;

    @Column(name = "verified_at")
    private Instant verifiedAt;
}

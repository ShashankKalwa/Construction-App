package com.example.contruction_management_app.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "contractor_bills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractorBill {

    @Id
    @Column(length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "contractor_name", nullable = false, length = 150)
    private String contractorName;

    @Column(name = "bill_number", nullable = false, length = 50)
    private String billNumber;

    @Column(name = "bill_date", nullable = false)
    private LocalDate billDate;

    @Column(name = "gross_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal grossAmount;

    @Column(name = "retention_pct", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal retentionPct = BigDecimal.valueOf(5.00);

    @Column(name = "retention_amount", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal retentionAmount = BigDecimal.ZERO;

    @Column(name = "tds_pct", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal tdsPct = BigDecimal.valueOf(2.00);

    @Column(name = "tds_amount", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal tdsAmount = BigDecimal.ZERO;

    @Column(name = "net_payable", nullable = false, precision = 14, scale = 2)
    private BigDecimal netPayable;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private BillPaymentStatus status = BillPaymentStatus.PENDING;

    @Column(name = "work_description", columnDefinition = "TEXT")
    private String workDescription;

    @Column(name = "certified_by", nullable = false, length = 50)
    private String certifiedBy;

    @Column(name = "created_at")
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PrePersist
    public void ensureId() {
        if (id == null || id.isBlank()) {
            this.id = "bill_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        }
        if (createdAt == null) {
            this.createdAt = Instant.now();
        }
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
}

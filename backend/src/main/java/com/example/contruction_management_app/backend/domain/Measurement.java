package com.example.contruction_management_app.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "measurements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Measurement {

    @Id
    @Column(length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boq_item_id")
    private BoqItem boqItem;

    @Column(name = "item_description", nullable = false, columnDefinition = "TEXT")
    private String itemDescription;

    @Column(name = "number_of_units", nullable = false, precision = 10, scale = 2)
    private BigDecimal numberOfUnits;

    @Column(name = "length_m", precision = 10, scale = 3)
    private BigDecimal lengthM;

    @Column(name = "width_m", precision = 10, scale = 3)
    private BigDecimal widthM;

    @Column(name = "depth_m", precision = 10, scale = 3)
    private BigDecimal depthM;

    @Column(name = "deduction_m3", nullable = false, precision = 10, scale = 3)
    private BigDecimal deductionM3;

    @Column(name = "net_quantity", nullable = false, precision = 12, scale = 3)
    private BigDecimal netQuantity;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(name = "location_details", length = 255)
    private String locationDetails;

    @Column(name = "measured_by", length = 50)
    private String measuredBy;

    @CreationTimestamp
    @Column(name = "measured_at", nullable = false, updatable = false)
    private Instant measuredAt;
}

package com.example.contruction_management_app.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "boq_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoqItem {

    @Id
    @Column(length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boq_id", nullable = false)
    private Boq boq;

    @Column(name = "item_code", nullable = false, length = 32)
    private String itemCode;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 64)
    private String category;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(name = "estimated_quantity", nullable = false, precision = 12, scale = 3)
    private BigDecimal estimatedQuantity;

    @Column(name = "unit_rate", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitRate;

    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "calculation_id", length = 50)
    private String calculationId;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;
}

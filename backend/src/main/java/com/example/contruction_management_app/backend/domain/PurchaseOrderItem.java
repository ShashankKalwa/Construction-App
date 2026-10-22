package com.example.contruction_management_app.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "purchase_order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderItem {

    @Id
    @Column(length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(name = "ordered_quantity", nullable = false, precision = 14, scale = 2)
    private BigDecimal orderedQuantity;

    @Column(name = "unit_price", nullable = false, precision = 14, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "received_quantity", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal receivedQuantity = BigDecimal.ZERO;

    @PrePersist
    public void ensureId() {
        if (id == null || id.isBlank()) {
            this.id = "poi_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        }
    }
}

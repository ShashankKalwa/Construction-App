package com.example.contruction_management_app.backend.repository;

import com.example.contruction_management_app.backend.domain.PurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, String> {
    List<PurchaseOrderItem> findByPurchaseOrderId(String purchaseOrderId);
}

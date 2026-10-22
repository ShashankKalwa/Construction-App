package com.example.contruction_management_app.backend.repository;

import com.example.contruction_management_app.backend.domain.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, String> {
    List<PurchaseOrder> findByProjectIdOrderByOrderDateDesc(String projectId);
    Optional<PurchaseOrder> findByPoNumber(String poNumber);
}

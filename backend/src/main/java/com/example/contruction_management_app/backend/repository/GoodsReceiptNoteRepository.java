package com.example.contruction_management_app.backend.repository;

import com.example.contruction_management_app.backend.domain.GoodsReceiptNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoodsReceiptNoteRepository extends JpaRepository<GoodsReceiptNote, String> {
    List<GoodsReceiptNote> findByPurchaseOrderIdOrderByReceivedDateDesc(String purchaseOrderId);
}

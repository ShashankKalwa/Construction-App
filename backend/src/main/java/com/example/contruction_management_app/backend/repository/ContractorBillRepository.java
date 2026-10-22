package com.example.contruction_management_app.backend.repository;

import com.example.contruction_management_app.backend.domain.ContractorBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractorBillRepository extends JpaRepository<ContractorBill, String> {
    List<ContractorBill> findByProjectIdOrderByBillDateDesc(String projectId);
}

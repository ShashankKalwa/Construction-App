package com.example.contruction_management_app.backend.repository;

import com.example.contruction_management_app.backend.domain.BoqItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoqItemRepository extends JpaRepository<BoqItem, String> {
    List<BoqItem> findByBoqIdOrderByOrderIndexAsc(String boqId);
}

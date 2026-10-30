package com.example.contruction_management_app.backend.repository;

import com.example.contruction_management_app.backend.domain.SitePhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SitePhotoRepository extends JpaRepository<SitePhoto, String> {
    List<SitePhoto> findByProjectIdOrderByCapturedAtDesc(String projectId);
}

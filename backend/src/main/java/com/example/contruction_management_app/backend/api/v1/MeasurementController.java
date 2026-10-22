package com.example.contruction_management_app.backend.api.v1;

import com.example.contruction_management_app.backend.dto.MeasurementCreateDto;
import com.example.contruction_management_app.backend.dto.MeasurementDto;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import com.example.contruction_management_app.backend.service.MeasurementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class MeasurementController {

    private final MeasurementService measurementService;

    public MeasurementController(MeasurementService measurementService) {
        this.measurementService = measurementService;
    }

    @PostMapping("/projects/{projectId}/measurements")
    @PreAuthorize("hasAuthority('measurements:take') or hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<MeasurementDto> recordMeasurement(
        @PathVariable String projectId,
        @Valid @RequestBody MeasurementCreateDto dto,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        MeasurementDto measurement = measurementService.recordMeasurement(projectId, dto, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(measurement);
    }

    @GetMapping("/projects/{projectId}/measurements")
    public ResponseEntity<List<MeasurementDto>> getMeasurementsForProject(
        @PathVariable String projectId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<MeasurementDto> list = measurementService.getMeasurementsForProject(projectId, principal);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/boq-items/{boqItemId}/measurements")
    public ResponseEntity<List<MeasurementDto>> getMeasurementsForBoqItem(
        @PathVariable String boqItemId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<MeasurementDto> list = measurementService.getMeasurementsForBoqItem(boqItemId, principal);
        return ResponseEntity.ok(list);
    }
}

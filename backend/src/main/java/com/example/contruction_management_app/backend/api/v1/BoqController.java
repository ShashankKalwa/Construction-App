package com.example.contruction_management_app.backend.api.v1;

import com.example.contruction_management_app.backend.dto.*;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import com.example.contruction_management_app.backend.service.BoqService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class BoqController {

    private final BoqService boqService;

    public BoqController(BoqService boqService) {
        this.boqService = boqService;
    }

    @PostMapping("/projects/{projectId}/boqs")
    @PreAuthorize("hasAuthority('boq:edit') or hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<BoqDto> createBoq(
        @PathVariable String projectId,
        @Valid @RequestBody BoqCreateDto dto,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        BoqDto boq = boqService.createBoq(projectId, dto, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(boq);
    }

    @GetMapping("/projects/{projectId}/boqs")
    public ResponseEntity<List<BoqDto>> getBoqsForProject(
        @PathVariable String projectId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<BoqDto> list = boqService.getBoqsForProject(projectId, principal);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/boqs/{boqId}")
    public ResponseEntity<BoqDto> getBoqById(
        @PathVariable String boqId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        BoqDto boq = boqService.getBoqById(boqId, principal);
        return ResponseEntity.ok(boq);
    }

    @PostMapping("/boqs/{boqId}/items")
    @PreAuthorize("hasAuthority('boq:edit') or hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<BoqItemDto> addBoqItem(
        @PathVariable String boqId,
        @Valid @RequestBody BoqItemCreateDto dto,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        BoqItemDto item = boqService.addBoqItem(boqId, dto, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @PostMapping("/boqs/{boqId}/submit")
    @PreAuthorize("hasAuthority('boq:edit') or hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<BoqDto> submitBoq(
        @PathVariable String boqId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        BoqDto boq = boqService.submitBoq(boqId, principal);
        return ResponseEntity.ok(boq);
    }

    @PostMapping("/boqs/{boqId}/decision")
    @PreAuthorize("hasAuthority('boq:approve') or hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<BoqDto> decideBoq(
        @PathVariable String boqId,
        @Valid @RequestBody BoqDecisionDto decisionDto,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        BoqDto boq = boqService.decideBoq(boqId, decisionDto, principal);
        return ResponseEntity.ok(boq);
    }
}

package com.example.contruction_management_app.backend;

import com.example.contruction_management_app.backend.domain.*;
import com.example.contruction_management_app.backend.dto.*;
import com.example.contruction_management_app.backend.exception.ForbiddenException;
import com.example.contruction_management_app.backend.repository.*;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import com.example.contruction_management_app.backend.service.BoqService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class BoqServiceTest {

    private BoqRepository boqRepository;
    private BoqItemRepository boqItemRepository;
    private ProjectRepository projectRepository;
    private ProjectMemberRepository projectMemberRepository;
    private BoqService boqService;

    private Project mockProject;
    private Organization mockOrg;
    private UserPrincipal engineerPrincipal;
    private UserPrincipal pmPrincipal;

    @BeforeEach
    void setUp() {
        boqRepository = Mockito.mock(BoqRepository.class);
        boqItemRepository = Mockito.mock(BoqItemRepository.class);
        projectRepository = Mockito.mock(ProjectRepository.class);
        projectMemberRepository = Mockito.mock(ProjectMemberRepository.class);

        boqService = new BoqService(boqRepository, boqItemRepository, projectRepository, projectMemberRepository);

        mockOrg = new Organization("Green Valley", "green-valley");
        mockOrg.setId("org_green_valley");

        mockProject = new Project("Green Valley Residency", "GVR-01", "Bengaluru", new BigDecimal("50000000"), mockOrg);
        mockProject.setId("prj_green_valley_residency");

        engineerPrincipal = new UserPrincipal(
            "usr_engineer", "engineer@greenvalley.com", "hash",
            "org_green_valley", "civil_engineer", List.of(), true
        );

        pmPrincipal = new UserPrincipal(
            "usr_pm", "pm@greenvalley.com", "hash",
            "org_green_valley", "project_manager", List.of(), true
        );

        when(projectRepository.findById("prj_green_valley_residency")).thenReturn(Optional.of(mockProject));
        when(projectMemberRepository.existsByProjectIdAndUserId(any(), any())).thenReturn(true);
    }

    @Test
    void testCreateBoq_Success() {
        when(boqRepository.findFirstByProjectIdOrderByVersionDesc("prj_green_valley_residency"))
            .thenReturn(Optional.empty());

        when(boqRepository.save(any(Boq.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BoqDto result = boqService.createBoq("prj_green_valley_residency", new BoqCreateDto("Substructure BOQ"), engineerPrincipal);

        assertNotNull(result);
        assertEquals(1, result.version());
        assertEquals(BoqStatus.DRAFT, result.status());
        assertEquals("usr_engineer", result.createdBy());
    }

    @Test
    void testAddBoqItem_CalculatesLineItemTotalAndBoqTotal() {
        Boq boq = Boq.builder()
            .id("boq_01")
            .project(mockProject)
            .title("Test BOQ")
            .version(1)
            .status(BoqStatus.DRAFT)
            .totalAmount(BigDecimal.ZERO)
            .createdBy("usr_engineer")
            .items(new ArrayList<>())
            .build();

        when(boqRepository.findById("boq_01")).thenReturn(Optional.of(boq));
        when(boqItemRepository.save(any(BoqItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(boqRepository.save(any(Boq.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BoqItemCreateDto itemDto = new BoqItemCreateDto(
            "UR-CONC", "M25 Concrete", "CONCRETE", "m³",
            BigDecimal.valueOf(10.0), BigDecimal.valueOf(6000.00), null
        );

        BoqItemDto itemResult = boqService.addBoqItem("boq_01", itemDto, engineerPrincipal);

        assertNotNull(itemResult);
        assertEquals(0, new BigDecimal("60000.00").compareTo(itemResult.totalAmount()));
        assertEquals(0, new BigDecimal("60000.00").compareTo(boq.getTotalAmount()));
    }

    @Test
    void testSelfApproval_IsDenied() {
        Boq boq = Boq.builder()
            .id("boq_01")
            .project(mockProject)
            .title("Test BOQ")
            .version(1)
            .status(BoqStatus.SUBMITTED)
            .totalAmount(BigDecimal.valueOf(50000.00))
            .createdBy("usr_engineer")
            .items(new ArrayList<>())
            .build();

        when(boqRepository.findById("boq_01")).thenReturn(Optional.of(boq));

        // Attempting to approve own BOQ as engineer
        assertThrows(ForbiddenException.class, () -> {
            boqService.decideBoq("boq_01", new BoqDecisionDto("APPROVED", "Looks good"), engineerPrincipal);
        });
    }

    @Test
    void testProjectManagerApproval_Success() {
        Boq boq = Boq.builder()
            .id("boq_01")
            .project(mockProject)
            .title("Test BOQ")
            .version(1)
            .status(BoqStatus.SUBMITTED)
            .totalAmount(BigDecimal.valueOf(50000.00))
            .createdBy("usr_engineer")
            .items(new ArrayList<>())
            .build();

        when(boqRepository.findById("boq_01")).thenReturn(Optional.of(boq));
        when(boqRepository.save(any(Boq.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Approved by Project Manager (different user)
        BoqDto approved = boqService.decideBoq("boq_01", new BoqDecisionDto("APPROVED", "Verified quantities"), pmPrincipal);

        assertNotNull(approved);
        assertEquals(BoqStatus.APPROVED, approved.status());
        assertEquals("usr_pm", approved.approvedBy());
    }
}

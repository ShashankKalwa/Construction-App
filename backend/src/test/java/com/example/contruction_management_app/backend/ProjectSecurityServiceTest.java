package com.example.contruction_management_app.backend;

import com.example.contruction_management_app.backend.domain.*;
import com.example.contruction_management_app.backend.repository.ProjectMemberRepository;
import com.example.contruction_management_app.backend.repository.ProjectRepository;
import com.example.contruction_management_app.backend.repository.RoleRepository;
import com.example.contruction_management_app.backend.security.ProjectSecurityService;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class ProjectSecurityServiceTest {

    private ProjectRepository projectRepository;
    private ProjectMemberRepository projectMemberRepository;
    private RoleRepository roleRepository;
    private ProjectSecurityService securityService;

    private Organization orgApex;
    private Organization orgOther;
    private Project projectA;
    private Role roleAdmin;
    private Role roleEngineer;

    @BeforeEach
    void setUp() {
        projectRepository = Mockito.mock(ProjectRepository.class);
        projectMemberRepository = Mockito.mock(ProjectMemberRepository.class);
        roleRepository = Mockito.mock(RoleRepository.class);
        securityService = new ProjectSecurityService(projectRepository, projectMemberRepository, roleRepository);

        orgApex = new Organization("Chaitanya InfraTech", "apex-infra");
        orgApex.setId("org_apex_infra");

        orgOther = new Organization("Other Builders", "other-builders");
        orgOther.setId("org_other");

        projectA = new Project("Commercial Tower B", "CTB-02", "Whitefield", new BigDecimal("54000000"), orgApex);
        projectA.setId("prj_commercial_tower_b");

        Permission permBoqEdit = new Permission("boq:edit", "Edit BOQ", "boq");
        Permission permExpensesApprove = new Permission("expenses:approve", "Approve Expenses", "finance");

        roleAdmin = new Role("company_admin", "Company Admin", "Org Admin");
        roleAdmin.setPermissions(Set.of(permBoqEdit, permExpensesApprove));

        roleEngineer = new Role("civil_engineer", "Civil Engineer", "Engineer");
        roleEngineer.setPermissions(Set.of(permBoqEdit));

        when(projectRepository.findById("prj_commercial_tower_b")).thenReturn(Optional.of(projectA));
        when(roleRepository.findById("company_admin")).thenReturn(Optional.of(roleAdmin));
        when(roleRepository.findById("civil_engineer")).thenReturn(Optional.of(roleEngineer));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateUser(String userId, String orgId, String roleId) {
        UserPrincipal principal = new UserPrincipal(
            userId, userId + "@test.com", "hash", orgId, roleId, List.of(), true
        );
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testCompanyAdmin_CanAccessProjectWithPermission() {
        authenticateUser("usr_admin", "org_apex_infra", "company_admin");

        assertTrue(securityService.can("prj_commercial_tower_b", "boq:edit"));
        assertTrue(securityService.can("prj_commercial_tower_b", "expenses:approve"));
        assertFalse(securityService.can("prj_commercial_tower_b", "non_existent_perm"));
    }

    @Test
    void testCrossOrganization_AccessIsDenied() {
        // User belongs to org_other, project belongs to org_apex_infra
        authenticateUser("usr_stranger", "org_other", "company_admin");

        assertFalse(securityService.can("prj_commercial_tower_b", "boq:edit"));
    }

    @Test
    void testNonExistentProject_ReturnsFalse() {
        authenticateUser("usr_admin", "org_apex_infra", "company_admin");

        assertFalse(securityService.can("prj_invalid_id", "boq:edit"));
    }

    @Test
    void testProjectMember_GrantedWhenAssignedRoleHasPermission() {
        authenticateUser("usr_eng", "org_apex_infra", "civil_engineer");

        User engUser = new User();
        engUser.setId("usr_eng");
        ProjectMember activeMember = new ProjectMember(projectA, engUser, "Civil Engineer", orgApex, "ACTIVE");

        when(projectMemberRepository.findByProjectIdAndUserId("prj_commercial_tower_b", "usr_eng"))
            .thenReturn(Optional.of(activeMember));

        // Engineer has boq:edit
        assertTrue(securityService.can("prj_commercial_tower_b", "boq:edit"));
        // Engineer does NOT have expenses:approve
        assertFalse(securityService.can("prj_commercial_tower_b", "expenses:approve"));
    }

    @Test
    void testInactiveProjectMember_IsDenied() {
        authenticateUser("usr_eng_inactive", "org_apex_infra", "civil_engineer");

        User engUser = new User();
        engUser.setId("usr_eng_inactive");
        ProjectMember suspendedMember = new ProjectMember(projectA, engUser, "Civil Engineer", orgApex, "SUSPENDED");

        when(projectMemberRepository.findByProjectIdAndUserId("prj_commercial_tower_b", "usr_eng_inactive"))
            .thenReturn(Optional.of(suspendedMember));

        assertFalse(securityService.can("prj_commercial_tower_b", "boq:edit"));
    }

    @Test
    void testCannotApproveOwn() {
        authenticateUser("usr_pm", "org_apex_infra", "project_manager");

        // Submitter is same user
        assertFalse(securityService.cannotApproveOwn("usr_pm"));

        // Submitter is another user
        assertTrue(securityService.cannotApproveOwn("usr_supervisor"));
    }
}

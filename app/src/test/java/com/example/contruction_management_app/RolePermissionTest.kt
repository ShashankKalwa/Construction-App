package com.example.contruction_management_app

import com.example.contruction_management_app.domain.model.Permission
import com.example.contruction_management_app.domain.model.Role
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RolePermissionTest {

    @Test
    fun superAdmin_hasAllPermissions() {
        val superAdmin = Role.SUPER_ADMIN
        for (permission in Permission.entries) {
            assertTrue("Super Admin should have permission $permission", superAdmin.hasPermission(permission))
        }
    }

    @Test
    fun civilEngineer_hasCalculatorsAndBoq_butCannotApproveExpenses() {
        val engineer = Role.CIVIL_ENGINEER
        assertTrue(engineer.hasPermission(Permission.USE_CALCULATORS))
        assertTrue(engineer.hasPermission(Permission.TAKE_MEASUREMENTS))
        assertTrue(engineer.hasPermission(Permission.VIEW_BOQ))
        assertTrue(engineer.hasPermission(Permission.EDIT_BOQ))

        assertFalse(engineer.hasPermission(Permission.APPROVE_EXPENSES))
        assertFalse(engineer.hasPermission(Permission.PROCESS_PAYMENTS))
    }

    @Test
    fun siteSupervisor_hasDailyReport_cannotApproveBoq() {
        val supervisor = Role.SITE_SUPERVISOR
        assertTrue(supervisor.hasPermission(Permission.DAILY_REPORT_WRITE))
        assertTrue(supervisor.hasPermission(Permission.LABOR_ATTENDANCE_WRITE))
        assertTrue(supervisor.hasPermission(Permission.REQUEST_MATERIAL))

        assertFalse(supervisor.hasPermission(Permission.APPROVE_BOQ))
        assertFalse(supervisor.hasPermission(Permission.APPROVE_EXPENSES))
    }

    @Test
    fun storekeeper_hasInventoryControl_cannotCreateProjects() {
        val storekeeper = Role.STOREKEEPER
        assertTrue(storekeeper.hasPermission(Permission.VIEW_INVENTORY))
        assertTrue(storekeeper.hasPermission(Permission.ISSUE_MATERIAL))
        assertTrue(storekeeper.hasPermission(Permission.RECEIVE_GOODS))

        assertFalse(storekeeper.hasPermission(Permission.CREATE_PROJECTS))
        assertFalse(storekeeper.hasPermission(Permission.SUBMIT_EXPENSE))
    }

    @Test
    fun roleFromId_mapsCorrectlyWithFallback() {
        assertEquals(Role.PROJECT_MANAGER, Role.fromId("project_manager"))
        assertEquals(Role.CIVIL_ENGINEER, Role.fromId("civil_engineer"))
        assertEquals(Role.SITE_SUPERVISOR, Role.fromId("unknown_role_fallback"))
    }
}

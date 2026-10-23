package com.example.contruction_management_app.data.repository

import android.content.Context
import com.example.contruction_management_app.core.auth.SessionDataStore
import com.example.contruction_management_app.domain.model.Role
import com.example.contruction_management_app.domain.model.User
import com.example.contruction_management_app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class UserRepositoryImpl(
    private val context: Context,
    private val sessionDataStore: SessionDataStore = SessionDataStore(context)
) : UserRepository {

    // Centralized Data Layer Registry for all standard organization users and roles
    private val dummyUsersRegistry: Map<Role, User> = mapOf(
        Role.SUPER_ADMIN to User(
            id = "usr_super_admin_01",
            email = "superadmin@apexinfra.com",
            fullName = "Vikram Malhotra",
            organizationId = "org_apex_infra",
            organizationName = "Apex Infra Ltd",
            role = Role.SUPER_ADMIN,
            assignedProjectIds = listOf("prj_green_valley_residency", "prj_skyline_commercial")
        ),
        Role.COMPANY_ADMIN to User(
            id = "usr_company_admin_02",
            email = "companyadmin@apexinfra.com",
            fullName = "Rajesh Sharma",
            organizationId = "org_apex_infra",
            organizationName = "Apex Infra Ltd",
            role = Role.COMPANY_ADMIN,
            assignedProjectIds = listOf("prj_green_valley_residency", "prj_skyline_commercial")
        ),
        Role.PROJECT_MANAGER to User(
            id = "usr_pm_03",
            email = "pm@apexinfra.com",
            fullName = "Priya Nair",
            organizationId = "org_apex_infra",
            organizationName = "Apex Infra Ltd",
            role = Role.PROJECT_MANAGER,
            assignedProjectIds = listOf("prj_green_valley_residency")
        ),
        Role.CIVIL_ENGINEER to User(
            id = "usr_engineer_04",
            email = "engineer@apexinfra.com",
            fullName = "Anand Verma",
            organizationId = "org_apex_infra",
            organizationName = "Apex Infra Ltd",
            role = Role.CIVIL_ENGINEER,
            assignedProjectIds = listOf("prj_green_valley_residency")
        ),
        Role.SITE_SUPERVISOR to User(
            id = "usr_supervisor_05",
            email = "supervisor@apexinfra.com",
            fullName = "Shashank Kumar",
            organizationId = "org_apex_infra",
            organizationName = "Apex Infra Ltd",
            role = Role.SITE_SUPERVISOR,
            assignedProjectIds = listOf("prj_green_valley_residency")
        ),
        Role.STOREKEEPER to User(
            id = "usr_storekeeper_06",
            email = "storekeeper@apexinfra.com",
            fullName = "Ramesh Yadav",
            organizationId = "org_apex_infra",
            organizationName = "Apex Infra Ltd",
            role = Role.STOREKEEPER,
            assignedProjectIds = listOf("prj_green_valley_residency")
        ),
        Role.ACCOUNTANT to User(
            id = "usr_accountant_07",
            email = "accountant@apexinfra.com",
            fullName = "Sunita Roy",
            organizationId = "org_apex_infra",
            organizationName = "Apex Infra Ltd",
            role = Role.ACCOUNTANT,
            assignedProjectIds = listOf("prj_green_valley_residency", "prj_skyline_commercial")
        ),
        Role.CONTRACTOR to User(
            id = "usr_contractor_08",
            email = "contractor@apexinfra.com",
            fullName = "Harpreet Singh",
            organizationId = "org_apex_infra",
            organizationName = "Apex Infra Ltd",
            role = Role.CONTRACTOR,
            assignedProjectIds = listOf("prj_green_valley_residency")
        ),
        Role.CLIENT_OWNER to User(
            id = "usr_client_09",
            email = "client@apexinfra.com",
            fullName = "Deepak Singhal",
            organizationId = "org_apex_infra",
            organizationName = "Apex Infra Ltd",
            role = Role.CLIENT_OWNER,
            assignedProjectIds = listOf("prj_green_valley_residency")
        )
    )

    override suspend fun getAllDummyUsers(): List<User> {
        return dummyUsersRegistry.values.toList()
    }

    override suspend fun getUserByRole(role: Role): User {
        return dummyUsersRegistry[role] ?: dummyUsersRegistry.getValue(Role.SUPER_ADMIN)
    }

    override suspend fun authenticateUser(email: String, role: Role): Result<User> {
        return try {
            val baseUser = getUserByRole(role)
            val userToSave = if (email.isNotBlank() && email != baseUser.email) {
                baseUser.copy(email = email)
            } else {
                baseUser
            }

            sessionDataStore.saveSession(
                token = "mock_jwt_token_${userToSave.id}",
                user = userToSave
            )
            Result.success(userToSave)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUser(): Flow<User?> {
        return sessionDataStore.currentUserFlow
    }

    override suspend fun logout() {
        sessionDataStore.clearSession()
    }
}

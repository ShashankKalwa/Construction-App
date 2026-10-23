package com.example.contruction_management_app.data.repository

import android.content.Context
import com.example.contruction_management_app.core.auth.SessionDataStore
import com.example.contruction_management_app.core.network.CmsApiService
import com.example.contruction_management_app.core.network.LoginNetworkRequest
import com.example.contruction_management_app.core.network.NetworkClient
import com.example.contruction_management_app.domain.model.Role
import com.example.contruction_management_app.domain.model.RoleCredential
import com.example.contruction_management_app.domain.model.User
import com.example.contruction_management_app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class UserRepositoryImpl(
    private val context: Context,
    private val sessionDataStore: SessionDataStore = SessionDataStore(context),
    private val apiService: CmsApiService = NetworkClient.getApiService(context)
) : UserRepository {


    companion object {
        const val MASTER_DEMO_PASSWORD = "Apex@123"
    }

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

    // Role-specific usernames and passwords
    private val dummyCredentialsRegistry: Map<Role, RoleCredential> = mapOf(
        Role.SUPER_ADMIN to RoleCredential(
            role = Role.SUPER_ADMIN,
            username = "superadmin",
            email = "superadmin@apexinfra.com",
            password = "Admin@123",
            description = "Full console access across all projects and governance"
        ),
        Role.COMPANY_ADMIN to RoleCredential(
            role = Role.COMPANY_ADMIN,
            username = "companyadmin",
            email = "companyadmin@apexinfra.com",
            password = "Company@123",
            description = "Enterprise operations and portfolio administration"
        ),
        Role.PROJECT_MANAGER to RoleCredential(
            role = Role.PROJECT_MANAGER,
            username = "pm",
            email = "pm@apexinfra.com",
            password = "Manager@123",
            description = "Project delivery, schedule tracking & stage approvals"
        ),
        Role.CIVIL_ENGINEER to RoleCredential(
            role = Role.CIVIL_ENGINEER,
            username = "engineer",
            email = "engineer@apexinfra.com",
            password = "Engineer@123",
            description = "Structural calculations, drawing reviews and BOQ audits"
        ),
        Role.SITE_SUPERVISOR to RoleCredential(
            role = Role.SITE_SUPERVISOR,
            username = "supervisor",
            email = "supervisor@apexinfra.com",
            password = "Site@123",
            description = "Daily site log, labor headcounts and obstacle logging"
        ),
        Role.STOREKEEPER to RoleCredential(
            role = Role.STOREKEEPER,
            username = "storekeeper",
            email = "storekeeper@apexinfra.com",
            password = "Store@123",
            description = "Material GRN, dispatch slips and warehouse inventory"
        ),
        Role.ACCOUNTANT to RoleCredential(
            role = Role.ACCOUNTANT,
            username = "accountant",
            email = "accountant@apexinfra.com",
            password = "Accounts@123",
            description = "Cashflow forecasting, contractor RA bills & invoices"
        ),
        Role.CONTRACTOR to RoleCredential(
            role = Role.CONTRACTOR,
            username = "contractor",
            email = "contractor@apexinfra.com",
            password = "Contract@123",
            description = "Work order milestones, labor allocation & claim requests"
        ),
        Role.CLIENT_OWNER to RoleCredential(
            role = Role.CLIENT_OWNER,
            username = "client",
            email = "client@apexinfra.com",
            password = "Client@123",
            description = "Executive milestone dashboard, photographic updates & audit"
        )
    )

    override suspend fun getAllDummyUsers(): List<User> {
        return dummyUsersRegistry.values.toList()
    }

    override suspend fun getUserByRole(role: Role): User {
        return dummyUsersRegistry[role] ?: dummyUsersRegistry.getValue(Role.SUPER_ADMIN)
    }

    override fun getCredentialsForRole(role: Role): RoleCredential {
        return dummyCredentialsRegistry[role] ?: dummyCredentialsRegistry.getValue(Role.SUPER_ADMIN)
    }

    override fun getAllRoleCredentials(): List<RoleCredential> {
        return dummyCredentialsRegistry.values.toList()
    }

    override suspend fun authenticateWithCredentials(
        username: String,
        password: String,
        selectedRole: Role
    ): Result<User> {
        val trimmedUsername = username.trim()
        val trimmedPassword = password.trim()

        if (trimmedUsername.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter your username or email."))
        }
        if (trimmedPassword.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter your password."))
        }

        val expectedCredential = getCredentialsForRole(selectedRole)

        // Validate password against role password or universal master demo password
        val isPasswordValid = trimmedPassword == expectedCredential.password ||
                trimmedPassword == MASTER_DEMO_PASSWORD ||
                trimmedPassword.equals("password123", ignoreCase = true)

        if (!isPasswordValid) {
            return Result.failure(
                IllegalArgumentException("Incorrect password for ${selectedRole.displayName}. Expected: ${expectedCredential.password}")
            )
        }

        val loginEmail = if (trimmedUsername.contains("@")) trimmedUsername else expectedCredential.email

        // 1. Attempt database authentication via live backend REST API
        try {
            val response = apiService.login(LoginNetworkRequest(loginEmail, trimmedPassword))
            if (response.isSuccessful && response.body() != null) {
                val loginBody = response.body()!!
                val netUser = loginBody.user
                val authenticatedUser = User(
                    id = netUser.id,
                    email = netUser.email,
                    fullName = netUser.fullName,
                    avatarUrl = netUser.avatarUrl,
                    organizationId = netUser.organizationId,
                    organizationName = netUser.organizationName.ifBlank { "Apex Infra Ltd" },
                    role = Role.fromId(netUser.role.id)
                )

                sessionDataStore.saveSession(
                    token = loginBody.accessToken,
                    user = authenticatedUser
                )
                return Result.success(authenticatedUser)
            }
        } catch (_: Exception) {
            // Network offline / timeout fallback: proceed with local offline session
        }

        // 2. Fallback to offline local session if network or backend is unreachable
        val baseUser = getUserByRole(selectedRole)
        val userToSave = if (trimmedUsername.contains("@")) {
            baseUser.copy(email = trimmedUsername)
        } else {
            baseUser
        }

        return try {
            sessionDataStore.saveSession(
                token = "mock_jwt_token_${userToSave.id}",
                user = userToSave
            )
            Result.success(userToSave)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun authenticateUser(email: String, role: Role): Result<User> {
        val credential = getCredentialsForRole(role)
        return authenticateWithCredentials(
            username = email.ifBlank { credential.username },
            password = credential.password,
            selectedRole = role
        )
    }

    override fun getCurrentUser(): Flow<User?> {
        return sessionDataStore.currentUserFlow
    }

    override suspend fun logout() {
        sessionDataStore.clearSession()
    }
}

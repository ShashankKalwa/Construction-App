package com.example.contruction_management_app.core.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.contruction_management_app.domain.model.Role
import com.example.contruction_management_app.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.example.contruction_management_app.core.network.CompanyMembershipNetworkDto
import com.example.contruction_management_app.core.network.ProjectMembershipNetworkDto
import com.example.contruction_management_app.core.network.UserMeNetworkResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cms_session")

class SessionDataStore(private val context: Context) {

    private val gson = Gson()

    companion object {
        val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
        val KEY_USER_ID = stringPreferencesKey("user_id")
        val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_USER_ROLE = stringPreferencesKey("user_role")
        val KEY_ORG_ID = stringPreferencesKey("org_id")
        val KEY_ORG_NAME = stringPreferencesKey("org_name")
        val KEY_ACTIVE_PROJECT_ID = stringPreferencesKey("active_project_id")
        val KEY_PERMISSIONS = stringSetPreferencesKey("effective_permissions")
        val KEY_PROJECT_MEMBERSHIPS = stringPreferencesKey("project_memberships_json")
        val KEY_COMPANY_MEMBERSHIPS = stringPreferencesKey("company_memberships_json")
    }

    val authTokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_AUTH_TOKEN]
    }

    val currentUserFlow: Flow<User?> = context.dataStore.data.map { preferences ->
        val id = preferences[KEY_USER_ID] ?: return@map null
        val email = preferences[KEY_USER_EMAIL] ?: ""
        val name = preferences[KEY_USER_NAME] ?: ""
        val roleId = preferences[KEY_USER_ROLE] ?: Role.SITE_SUPERVISOR.id
        val orgId = preferences[KEY_ORG_ID] ?: ""
        val orgName = preferences[KEY_ORG_NAME] ?: "Apex Infra Ltd"

        User(
            id = id,
            email = email,
            fullName = name,
            organizationId = orgId,
            organizationName = orgName,
            role = Role.fromId(roleId)
        )
    }

    val effectivePermissionsFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[KEY_PERMISSIONS] ?: emptySet()
    }

    val projectMembershipsFlow: Flow<List<ProjectMembershipNetworkDto>> = context.dataStore.data.map { preferences ->
        val json = preferences[KEY_PROJECT_MEMBERSHIPS] ?: return@map emptyList()
        try {
            val type = object : TypeToken<List<ProjectMembershipNetworkDto>>() {}.type
            gson.fromJson<List<ProjectMembershipNetworkDto>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    val companyMembershipsFlow: Flow<List<CompanyMembershipNetworkDto>> = context.dataStore.data.map { preferences ->
        val json = preferences[KEY_COMPANY_MEMBERSHIPS] ?: return@map emptyList()
        try {
            val type = object : TypeToken<List<CompanyMembershipNetworkDto>>() {}.type
            gson.fromJson<List<CompanyMembershipNetworkDto>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveSession(token: String, user: User) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AUTH_TOKEN] = token
            preferences[KEY_USER_ID] = user.id
            preferences[KEY_USER_EMAIL] = user.email
            preferences[KEY_USER_NAME] = user.fullName
            preferences[KEY_USER_ROLE] = user.role.id
            preferences[KEY_ORG_ID] = user.organizationId
            preferences[KEY_ORG_NAME] = user.organizationName
        }
    }

    suspend fun saveUserMe(userMe: UserMeNetworkResponse) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_ID] = userMe.id
            preferences[KEY_USER_EMAIL] = userMe.email
            preferences[KEY_USER_NAME] = userMe.fullName
            preferences[KEY_USER_ROLE] = userMe.primaryRole.id
            preferences[KEY_ORG_ID] = userMe.activeCompanyId
            preferences[KEY_ORG_NAME] = userMe.activeCompanyName
            preferences[KEY_PERMISSIONS] = userMe.effectivePermissions.toSet()
            preferences[KEY_PROJECT_MEMBERSHIPS] = gson.toJson(userMe.projectMemberships)
            preferences[KEY_COMPANY_MEMBERSHIPS] = gson.toJson(userMe.companyMemberships)

            // If active project is not set, select the first assigned project
            if (preferences[KEY_ACTIVE_PROJECT_ID] == null && userMe.projectMemberships.isNotEmpty()) {
                preferences[KEY_ACTIVE_PROJECT_ID] = userMe.projectMemberships.first().projectId
            }
        }
    }

    val activeProjectIdFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_ACTIVE_PROJECT_ID]
    }

    suspend fun setActiveProjectId(projectId: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ACTIVE_PROJECT_ID] = projectId
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

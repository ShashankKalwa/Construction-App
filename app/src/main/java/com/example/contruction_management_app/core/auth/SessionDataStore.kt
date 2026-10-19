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

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cms_session")

class SessionDataStore(private val context: Context) {

    companion object {
        val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
        val KEY_USER_ID = stringPreferencesKey("user_id")
        val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_USER_ROLE = stringPreferencesKey("user_role")
        val KEY_ORG_ID = stringPreferencesKey("org_id")
        val KEY_ORG_NAME = stringPreferencesKey("org_name")
        val KEY_ACTIVE_PROJECT_ID = stringPreferencesKey("active_project_id")
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

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

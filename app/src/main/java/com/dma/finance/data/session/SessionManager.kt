package com.dma.finance.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionDataStore by preferencesDataStore(name = "session")

/**
 * Conserve l'utilisateur actuellement connecté et le projet actif sélectionné,
 * afin que l'application puisse plusieurs profils/utilisateurs sur le même appareil
 * et se souvenir du dernier projet ouvert par chacun.
 */
@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val CURRENT_USER_ID = longPreferencesKey("current_user_id")
        val CURRENT_PROJECT_ID = longPreferencesKey("current_project_id")
    }

    val currentUserId: Flow<Long?> = context.sessionDataStore.data.map { prefs ->
        prefs[Keys.CURRENT_USER_ID]?.takeIf { it > 0 }
    }

    val currentProjectId: Flow<Long?> = context.sessionDataStore.data.map { prefs ->
        prefs[Keys.CURRENT_PROJECT_ID]?.takeIf { it > 0 }
    }

    suspend fun setCurrentUser(userId: Long) {
        context.sessionDataStore.edit { prefs ->
            prefs[Keys.CURRENT_USER_ID] = userId
        }
    }

    suspend fun setCurrentProject(projectId: Long) {
        context.sessionDataStore.edit { prefs ->
            prefs[Keys.CURRENT_PROJECT_ID] = projectId
        }
    }

    suspend fun clearCurrentProject() {
        context.sessionDataStore.edit { prefs ->
            prefs.remove(Keys.CURRENT_PROJECT_ID)
        }
    }

    /** Déconnexion : on oublie l'utilisateur et le projet actif. */
    suspend fun signOut() {
        context.sessionDataStore.edit { prefs ->
            prefs.remove(Keys.CURRENT_USER_ID)
            prefs.remove(Keys.CURRENT_PROJECT_ID)
        }
    }
}

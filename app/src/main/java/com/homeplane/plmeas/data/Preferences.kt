package com.homeplane.plmeas.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "home_planner_prefs")

data class AppPreferences(
    val onboardingCompleted: Boolean = false,
    val activeProjectId: Long = -1L,
    val currency: String = "USD",
    val units: String = "m",
    val isDarkTheme: Boolean = false,
    val notificationsEnabled: Boolean = true
)

object PreferenceKeys {
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    val ACTIVE_PROJECT_ID = longPreferencesKey("active_project_id")
    val CURRENCY = stringPreferencesKey("currency")
    val UNITS = stringPreferencesKey("units")
    val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
    val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
}

class PreferencesManager(private val context: Context) {

    val appPreferences: Flow<AppPreferences> = context.dataStore.data.map { prefs ->
        AppPreferences(
            onboardingCompleted = prefs[PreferenceKeys.ONBOARDING_COMPLETED] ?: false,
            activeProjectId = prefs[PreferenceKeys.ACTIVE_PROJECT_ID] ?: -1L,
            currency = prefs[PreferenceKeys.CURRENCY] ?: "USD",
            units = prefs[PreferenceKeys.UNITS] ?: "m",
            isDarkTheme = prefs[PreferenceKeys.IS_DARK_THEME] ?: false,
            notificationsEnabled = prefs[PreferenceKeys.NOTIFICATIONS_ENABLED] ?: true
        )
    }

    suspend fun setOnboardingCompleted(value: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.ONBOARDING_COMPLETED] = value }
    }

    suspend fun setActiveProjectId(value: Long) {
        context.dataStore.edit { it[PreferenceKeys.ACTIVE_PROJECT_ID] = value }
    }

    suspend fun setCurrency(value: String) {
        context.dataStore.edit { it[PreferenceKeys.CURRENCY] = value }
    }

    suspend fun setUnits(value: String) {
        context.dataStore.edit { it[PreferenceKeys.UNITS] = value }
    }

    suspend fun setDarkTheme(value: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.IS_DARK_THEME] = value }
    }

    suspend fun setNotificationsEnabled(value: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.NOTIFICATIONS_ENABLED] = value }
    }
}

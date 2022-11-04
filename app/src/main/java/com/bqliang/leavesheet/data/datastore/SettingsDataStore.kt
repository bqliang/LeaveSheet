package com.bqliang.leavesheet.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.bqliang.leavesheet.MyApp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private const val SETTINGS_DATA_STORE_NAME = "settings_data_store"

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = SETTINGS_DATA_STORE_NAME
)


object SettingsDataStore {
    private val settingsDataStore by lazy { MyApp.context.settingsDataStore }

    private val FACULTY_AUDIT_VISIBLE = booleanPreferencesKey("faculty_audit_visible")

    val facultyAuditVisible: Flow<Boolean> = settingsDataStore.data
        .catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[FACULTY_AUDIT_VISIBLE] ?: true
        }

    suspend fun saveFacultyAuditVisible(value: Boolean) {
        settingsDataStore.edit { preferences ->
            preferences[FACULTY_AUDIT_VISIBLE] = value
        }
    }
}
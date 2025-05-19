package com.example.stockdash.data.repository

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

@Singleton
class UserPreferencesRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private object PreferencesKeys {
        val APP_THEME_MODE = intPreferencesKey("app_theme_mode")
    }

    val appThemeMode: Flow<Int> = context.dataStore.data
        .map { preferences ->
            // Default to FOLLOW_SYSTEM if no preference is set
            preferences[PreferencesKeys.APP_THEME_MODE] ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

    suspend fun setAppThemeMode(themeMode: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_THEME_MODE] = themeMode
        }
    }
}
package com.emagioda.myapp.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val STORE_NAME = "settings"

// Extension de Context (debe ser top-level)
val Context.dataStore by preferencesDataStore(name = STORE_NAME)
private val KEY_DARK_THEME = booleanPreferencesKey("dark_theme")

object ThemePreferences {
    fun observeDarkTheme(context: Context): Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[KEY_DARK_THEME] ?: false }

    suspend fun setDarkTheme(context: Context, enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_DARK_THEME] = enabled }
    }
}

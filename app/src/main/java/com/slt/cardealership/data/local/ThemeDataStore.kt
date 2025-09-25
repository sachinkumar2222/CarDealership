package com.slt.cardealership.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Create a DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class ThemeDataStore @Inject constructor(@ApplicationContext private val context: Context) {

    // A key to store the dark mode boolean
    private val isDarkKey = booleanPreferencesKey("is_dark_mode")

    // A flow that emits the current theme preference
    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[isDarkKey] ?: false // Default to false (light mode)
    }

    // A function to update the theme preference
    suspend fun setDarkMode(isDark: Boolean) {
        context.dataStore.edit { settings ->
            settings[isDarkKey] = isDark
        }
    }
}
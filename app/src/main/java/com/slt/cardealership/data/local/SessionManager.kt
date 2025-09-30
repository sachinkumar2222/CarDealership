package com.slt.cardealership.data.local

import javax.inject.Inject
import javax.inject.Singleton
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * A simple singleton class to hold the session token in memory.
 * In a production app, you would use encrypted SharedPreferences or DataStore for persistence.
 */
private val Context.dataStore by preferencesDataStore(name = "user_preferences")
@Singleton
class SessionManager @Inject constructor( @ApplicationContext private val context: Context) {
    var authToken: String? = null
    private val USER_DEALER_SLUG = stringPreferencesKey("user_dealer_slug")
    suspend fun getDealerSlug(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[USER_DEALER_SLUG]
        }.first()
    }
    suspend fun saveDealerSlug(slug: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_DEALER_SLUG] = slug
        }
    }

}

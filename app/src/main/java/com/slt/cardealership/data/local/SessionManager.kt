package com.slt.cardealership.data.local

import javax.inject.Inject
import javax.inject.Singleton
import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.auth0.android.jwt.JWT
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

    private val AUTH_TOKEN = stringPreferencesKey("auth_token")
    private val USER_DEALER_SLUG = stringPreferencesKey("user_dealer_slug")


    var authToken: String? = null

    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN] = token
        }
    }

    suspend fun getAuthToken(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[AUTH_TOKEN]
        }.first()
    }

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


    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun getDealerId(): Int? {
        val token = getAuthToken() ?: return null
        return try {
            val jwt = JWT(token)
            // *** Use the claim name from your logs ***
            val dealerIdString = jwt.getClaim("extension_DealerId").asString()
            val dealerId = dealerIdString?.toIntOrNull()
            Log.d("SessionManager", "Parsed Dealer ID (extension_DealerId): $dealerId")
            dealerId
        } catch (e: Exception) {
            Log.e("SessionManager", "Error parsing token for Dealer ID (extension_DealerId)", e)
            null
        }
    }

}

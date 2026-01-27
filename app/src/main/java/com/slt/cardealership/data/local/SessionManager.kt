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

    // This is in-memory cache, not used in your current setup
    // var authToken: String? = null

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

    /**
     * --- NEW HELPER FUNCTION ---
     * Gets the auth token and parses it into a JWT object.
     */
    private suspend fun getJwt(): JWT? {
        val token = getAuthToken() ?: return null
        return try {
            JWT(token)
        } catch (e: Exception) {
            Log.e("SessionManager", "Error parsing token", e)
            null
        }
    }

    /**
     * --- UPDATED FUNCTION ---
     * Now uses the getJwt() helper.
     */
    suspend fun getDealerId(): Int? {
        val jwt = getJwt() ?: return null // <-- Use helper
        return try {
            val dealerIdString = jwt.getClaim("extension_DealerId").asString()
            val dealerId = dealerIdString?.toIntOrNull()
            Log.d("SessionManager", "Parsed Dealer ID (extension_DealerId): $dealerId")
            dealerId
        } catch (e: Exception) {
            Log.e("SessionManager", "Error parsing Dealer ID claim", e)
            null
        }
    }

    suspend fun getEmail(): String? {
        val jwt = getJwt() ?: return null
        return try {
            jwt.getClaim("email").asString()
        } catch (e: Exception) {
            Log.e("SessionManager", "Error parsing email claim", e)
            null
        }
    }

    suspend fun getUsername(): String? {
        val jwt = getJwt() ?: return null
        return try {
            // Your JWT log shows the email in the "email" claim
            jwt.getClaim("email").asString()
        } catch (e: Exception) {
            Log.e("SessionManager", "Error parsing email claim", e)
            null
        }
    }

    /**
     * --- NEW FUNCTION ---
     */
    suspend fun getFirstName(): String? {
        val jwt = getJwt() ?: return null
        return try {
            // Your JWT log shows "given_name"
            jwt.getClaim("given_name").asString()
        } catch (e: Exception) {
            Log.e("SessionManager", "Error parsing given_name claim", e)
            null
        }
    }

    /**
     * --- NEW FUNCTION ---
     */
    suspend fun getLastName(): String? {
        val jwt = getJwt() ?: return null
        return try {
            jwt.getClaim("family_name").asString()
        } catch (e: Exception) {
            Log.e("SessionManager", "Error parsing family_name claim", e)
            null
        }
    }

    suspend fun getUserId(): Int? {
        val jwt = getJwt() ?: return null
        return try {
            // Attempt to read extension_UserId (common pattern with extension_DealerId)
            val userIdString = jwt.getClaim("extension_UserId").asString()
            userIdString?.toIntOrNull()
        } catch (e: Exception) {
            Log.e("SessionManager", "Error parsing User ID claim", e)
            null
        }
    }
}
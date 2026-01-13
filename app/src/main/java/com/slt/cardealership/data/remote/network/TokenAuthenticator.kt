package com.slt.cardealership.data.remote.network

import android.util.Log
import com.slt.cardealership.data.local.SessionManager
import com.slt.cardealership.domain.repo.AuthRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val sessionManager: SessionManager,
    private val authRepository: AuthRepository
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        Log.d("TokenAuthenticator", "401 Unauthorized error detected. Authenticator is running.")

        if (response.request.header("Authorization") == null) {
            Log.d("TokenAuthenticator", "Request didn't have an auth header. Not retrying.")
            return null
        }

        Log.d("TokenAuthenticator", "Attempting to acquire a new token silently...")

        // We wrap this in a try-catch to get the specific error from the repository
        try {
            val newToken = runBlocking {
                // We use .getOrThrow() here to make sure we catch the exception if it fails
                authRepository.acquireTokenSilent().getOrThrow()
            }

            Log.d("TokenAuthenticator", "SUCCESS: Acquired a new token.")
            runBlocking {
                sessionManager.saveAuthToken(newToken)
            }
            Log.d("TokenAuthenticator", "Retrying the original request with the new token.")
            return response.request.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()

        } catch (e: Exception) {

            Log.e("TokenAuthenticator", "FAILURE: An exception occurred during silent token refresh.", e)

            return null
        }
    }
}
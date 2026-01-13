package com.slt.cardealership.data.remote.network

import android.util.Log
import com.slt.cardealership.data.local.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {


    override fun intercept(chain: Interceptor.Chain): Response {
        Log.d("AuthInterceptor", "--- INTERCEPTOR IS RUNNING ---")

        // 1. Get the original token
        val originalToken = runBlocking {
            sessionManager.getAuthToken()
        }
        Log.d("AuthInterceptor", "Found token in SessionManager: $originalToken")

        // 2. Decide what token to send (the real one or the test one)
        var tokenToSend = originalToken

        val requestBuilder = chain.request().newBuilder()

        // 3. Add the (now corrupted) token to the header
        if (!tokenToSend.isNullOrBlank()) {
            Log.d("AuthInterceptor", "Adding Authorization header.")
            requestBuilder.addHeader("Authorization", "Bearer $tokenToSend")
        } else {
            Log.w("AuthInterceptor", "No token found. Proceeding without Authorization header.")
        }

        return chain.proceed(requestBuilder.build())
    }

}
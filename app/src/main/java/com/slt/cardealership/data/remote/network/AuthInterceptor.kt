package com.slt.cardealership.data.remote.network

import com.slt.cardealership.data.local.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var token = runBlocking {
            sessionManager.getAuthToken()
        }

        // --- TEMPORARY CODE FOR TESTING ---
        // This line forces a 401 error so you can test your TokenAuthenticator.
//        if (!token.isNullOrBlank()) {
//            token += "invalidate"
//        }
        // ------------------------------------

        val request = chain.request().newBuilder()
        if (!token.isNullOrBlank()) {
            request.addHeader("Authorization", "Bearer $token")
        }
        return chain.proceed(request.build())
    }
}
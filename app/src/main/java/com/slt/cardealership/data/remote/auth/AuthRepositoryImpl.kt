package com.slt.cardealership.data.remote.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import com.microsoft.identity.client.*
import com.microsoft.identity.client.exception.MsalException
import com.slt.cardealership.R
import com.slt.cardealership.data.local.SessionManager
import com.slt.cardealership.domain.repo.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Singleton
class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionManager: SessionManager
) : AuthRepository {

    private var msalApp: IPublicClientApplication? = null

    private suspend fun getMsalApp(): IPublicClientApplication = withContext(Dispatchers.IO) {
        if (msalApp == null) {
            msalApp = PublicClientApplication.create(context, R.raw.auth_config)
        }
        msalApp!!
    }

    private suspend fun getSignedInAccount(): IAccount? {
        val app = getMsalApp()
        return suspendCancellableCoroutine { continuation ->
            try {
                (app as? IMultipleAccountPublicClientApplication)?.getAccounts(object : IPublicClientApplication.LoadAccountsCallback {
                    override fun onTaskCompleted(result: MutableList<IAccount>?) {
                        continuation.resume(result?.firstOrNull())
                    }
                    override fun onError(exception: MsalException?) {
                        continuation.resumeWithException(exception ?: RuntimeException("Failed to load accounts"))
                    }
                }) ?: continuation.resume(null)
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }

    override suspend fun signIn(activity: Activity?): Result<String> {
        val app = getMsalApp()
        return suspendCoroutine { continuation ->
            val scopes = listOf(
                "https://sbamybuissness.onmicrosoft.com/23dbe00b-9486-4e40-be6c-3db22237ac57/tasks.read",
                "https://sbamybuissness.onmicrosoft.com/23dbe00b-9486-4e40-be6c-3db22237ac57/tasks.write"
            )
            val parameters = AcquireTokenParameters.Builder()
                .startAuthorizationFromActivity(activity)
                .withScopes(scopes)
                .withCallback(object : AuthenticationCallback {
                    override fun onSuccess(result: IAuthenticationResult) {
                        runBlocking { sessionManager.saveAuthToken(result.accessToken) }
                        continuation.resume(Result.success(result.accessToken))
                    }

                    override fun onError(exception: MsalException) {
                        continuation.resume(Result.failure(exception))
                    }

                    override fun onCancel() {
                        continuation.resume(Result.failure(CancellationException("User cancelled login")))
                    }
                })
                .build()
            app.acquireToken(parameters)
        }
    }

    override suspend fun acquireTokenSilent(): Result<String> {
        val currentAccount = getSignedInAccount()
            ?: return Result.failure(IllegalStateException("No cached account found."))

        val app = getMsalApp()

        val authority = app.configuration.defaultAuthority.authorityURL.toString()

        return suspendCoroutine { continuation ->
            val scopes = listOf(
                "https://sbamybuissness.onmicrosoft.com/23dbe00b-9486-4e40-be6c-3db22237ac57/tasks.read",
                "https://sbamybuissness.onmicrosoft.com/23dbe00b-9486-4e40-be6c-3db22237ac57/tasks.write"
            )
            val parameters = AcquireTokenSilentParameters.Builder()
                .forAccount(currentAccount)
                 .fromAuthority(authority) // This is correctly commented out
                .withScopes(scopes)
                .withCallback(object : SilentAuthenticationCallback {
                    override fun onSuccess(result: IAuthenticationResult) {
                        runBlocking { sessionManager.saveAuthToken(result.accessToken) }
                        continuation.resume(Result.success(result.accessToken))
                    }

                    override fun onError(exception: MsalException) {
                        continuation.resume(Result.failure(exception))
                    }
                })
                .build()
            app.acquireTokenSilentAsync(parameters)
        }
    }

    // In: AuthRepositoryImpl.kt
// FALLBACK IMPLEMENTATION - Use only if dependency update fails

    override suspend fun signOut(): Result<Unit> {
        return try {
            val app = getMsalApp()
            val currentAccount = getSignedInAccount()

            // If there is an account, remove it from MSAL
            if (currentAccount != null) {
                suspendCancellableCoroutine<Unit> { continuation ->
                    (app as? IMultipleAccountPublicClientApplication)?.removeAccount(
                        currentAccount,
                        object : IMultipleAccountPublicClientApplication.RemoveAccountCallback {
                            override fun onRemoved() {
                                continuation.resume(Unit)
                            }
                            override fun onError(exception: MsalException) {
                                continuation.resumeWithException(exception)
                            }
                        }
                    ) ?: continuation.resume(Unit) // Fallback for single account app
                }
            }

            // The most important step: ALWAYS clear your app's local storage
            runBlocking {
                sessionManager.clearSession()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
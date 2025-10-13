package com.slt.cardealership.domain.usecase


import android.app.Activity
import com.slt.cardealership.data.local.SessionManager
import com.slt.cardealership.domain.repo.AuthRepository

class SignInUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke(activity: Activity?) = repo.signIn(activity)
}

class SilentLoginUseCase(
    private val repo: AuthRepository,
    private val sessionManager: SessionManager // <-- Add SessionManager dependency
) {
    suspend operator fun invoke(): Result<String> {
        // 1. First, try to get the token we saved in DataStore.
        val savedToken = sessionManager.getAuthToken()
        if (!savedToken.isNullOrBlank()) {
            return Result.success(savedToken)
        }

        // 2. If no token is saved, fall back to the repository's silent method.
        return repo.acquireTokenSilent()
    }
}

class SignOutUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke() = repo.signOut()
}
package com.slt.cardealership.domain.usecase


import android.app.Activity
import com.slt.cardealership.domain.repo.AuthRepository

class SignInUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke(activity: Activity?) = repo.signIn(activity)
}

class SilentLoginUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke() = repo.acquireTokenSilent()
}

class SignOutUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke() = repo.signOut()
}
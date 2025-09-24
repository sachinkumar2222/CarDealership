package com.slt.cardealership.domain.repo

import android.app.Activity

interface AuthRepository {
    suspend fun signIn(activity: Activity?): Result<String> // returns access token
    suspend fun acquireTokenSilent(): Result<String>
    suspend fun signOut(): Result<Unit>
}

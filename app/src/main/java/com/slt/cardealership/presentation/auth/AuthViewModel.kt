package com.slt.cardealership.presentation.auth

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.common.ResultState
import com.slt.cardealership.data.local.SessionManager
import com.slt.cardealership.domain.model.User
import com.slt.cardealership.domain.usecase.SignInUseCase
import com.slt.cardealership.domain.usecase.SilentLoginUseCase
import com.slt.cardealership.domain.usecase.SignOutUseCase
import com.slt.cardealership.utils.TokenParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthEvent {
    data class NavigateToHome(val token: String) : AuthEvent()
    object NavigateToLogin : AuthEvent()
    data class ShowSnackbar(val message: String) : AuthEvent()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val silentLoginUseCase: SilentLoginUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _authState = MutableStateFlow<ResultState<String>>(ResultState.Loading)
    val authState: StateFlow<ResultState<String>> = _authState

    private val _userState = MutableStateFlow<User?>(null)
    val userState: StateFlow<User?> = _userState.asStateFlow()

    private val _events = Channel<AuthEvent>()
    val events = _events.receiveAsFlow()

    init {
        trySilentLoginOnAppStart()
    }

    fun signIn(activity: Activity?) {
        _authState.value = ResultState.Loading
        viewModelScope.launch {
            try {
                val result = signInUseCase(activity)
                result.fold(
                    onSuccess = { token ->
                        handleLoginSuccess(token)
                    },
                    onFailure = { e ->
                        handleLoginFailure(e)
                    }
                )
            } catch (e: Exception) {
                handleLoginFailure(e)
            }
        }
    }

    private fun trySilentLoginOnAppStart() {
        viewModelScope.launch {
            try {
                silentLoginUseCase().fold(
                    onSuccess = { token ->
                        handleLoginSuccess(token, isSilent = true)
                    },
                    onFailure = {
                        _events.send(AuthEvent.NavigateToLogin)
                    }
                )
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Silent login crashed", e)
                _events.send(AuthEvent.NavigateToLogin)
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _authState.value = ResultState.Loading
            
            // 1. Attempt remote sign-out (best effort)
            try {
                signOutUseCase()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Remote sign out failed", e)
                // We proceed anyway because we MUST log out locally
            }

            // 2. Clear local session data
            try {
                sessionManager.clearSession()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to clear session", e)
            }

            // 3. Reset local state
            _userState.value = null
            _authState.value = ResultState.Success("Signed out")

            // 4. Navigate to Login
            _events.send(AuthEvent.NavigateToLogin)
        }
    }

    private suspend fun handleLoginSuccess(token: String, isSilent: Boolean = false) {
        try {
            // Save token
            sessionManager.saveAuthToken(token)

            // Parse and save user info
            val user = TokenParser.parse(token)
            _userState.value = user

            TokenParser.getDealerIdFromToken(token)?.let { dealerId ->
                sessionManager.saveDealerSlug(dealerId.toString())
                if (!isSilent) {
                    Log.d("AuthViewModel", "Dealer slug SAVED on login: $dealerId")
                }
            }

            if (isSilent) {
                Log.d("AuthViewModel", "Silent login successful for: ${user?.name}")
            }

            _authState.value = ResultState.Success(token)
            _events.send(AuthEvent.NavigateToHome(token))

        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error handling login success", e)
            handleLoginFailure(e)
        }
    }

    private suspend fun handleLoginFailure(e: Throwable) {
        Log.e("AuthViewModel", "Login failed", e)
        _authState.value = ResultState.Error(e.message ?: "Unknown error")
        _events.send(AuthEvent.ShowSnackbar(e.message ?: "Login failed"))
    }
}
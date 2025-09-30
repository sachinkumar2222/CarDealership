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
            val result = signInUseCase(activity)
            _authState.value = result.fold(
                onSuccess = { token ->
                    val user = TokenParser.parse(token)
                    _userState.value = user

                    TokenParser.getDealerIdFromToken(token)?.let { dealerId ->
                        sessionManager.saveDealerSlug(dealerId.toString())
                        Log.d("AuthViewModel", "Dealer slug SAVED on login: $dealerId")
                    }

                    _events.send(AuthEvent.NavigateToHome(token))
                    ResultState.Success(token)
                },
                onFailure = { e ->
                    _events.send(AuthEvent.ShowSnackbar(e.message ?: "Unknown error"))
                    ResultState.Error(e.message ?: "Unknown error")
                }
            )
        }
    }

    private fun trySilentLoginOnAppStart() {
        viewModelScope.launch {
            silentLoginUseCase().fold(
                onSuccess = { token ->
                    // --- ALSO CALL THE PARSER HERE ---
                    val user = TokenParser.parse(token)
                    _userState.value = user // Store the parsed user
                    TokenParser.getDealerIdFromToken(token)?.let { dealerId ->
                        sessionManager.saveDealerSlug(dealerId.toString())
                        Log.d("AuthViewModel", "Dealer slug SAVED on silent login: $dealerId")
                    }

                    Log.d("AuthViewModel", "Silent login successful for: ${user?.name}")

                    _events.send(AuthEvent.NavigateToHome(token))
                },
                onFailure = {
                    _events.send(AuthEvent.NavigateToLogin)
                }
            )
        }
    }


    fun signOut() {
        viewModelScope.launch {
            val result = signOutUseCase()
            _authState.value = result.fold(
                onSuccess = {
                    _events.send(AuthEvent.ShowSnackbar("Signed out successfully"))
                    ResultState.Success("Signed out")
                },
                onFailure = { e ->
                    _events.send(AuthEvent.ShowSnackbar(e.message ?: "Sign out failed"))
                    ResultState.Error(e.message ?: "Sign out failed")
                }
            )
        }
    }
}
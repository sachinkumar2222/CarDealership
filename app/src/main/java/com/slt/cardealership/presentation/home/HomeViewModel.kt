package com.slt.cardealership.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.domain.repo.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    // This ViewModel can be used to handle logout logic,
    // user profile information for the top bar, etc.
    // For now, it's simple, but it's ready for future logic.

    fun onLogoutClicked() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}

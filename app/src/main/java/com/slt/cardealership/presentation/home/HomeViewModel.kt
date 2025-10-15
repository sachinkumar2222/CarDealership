package com.slt.cardealership.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.data.local.SessionManager
import com.slt.cardealership.domain.model.Post
import com.slt.cardealership.domain.repo.AuthRepository
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val latestPosts: List<Post>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dealerRepository: DealerRepository,
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    // This ViewModel can be used to handle logout logic,
    // user profile information for the top bar, etc.
    // For now, it's simple, but it's ready for future logic.

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        fetchLatestPost()
    }

    fun onLogoutClicked() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    private fun fetchLatestPost() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            val dealerId = sessionManager.getDealerSlug()?.toLongOrNull()
            if (dealerId == null) {
                _uiState.value = HomeUiState.Error("Dealer ID not found.")
                return@launch
            }

            dealerRepository.getPosts(dealerId)
                .onSuccess { posts ->
                    _uiState.value = HomeUiState.Success(posts)
                }
                .onFailure {
                    _uiState.value = HomeUiState.Error(it.message ?: "Failed to load posts.")
                }
        }
    }
}

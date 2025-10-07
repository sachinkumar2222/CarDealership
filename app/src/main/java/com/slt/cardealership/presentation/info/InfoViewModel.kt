package com.slt.cardealership.presentation.info

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.data.local.SessionManager
import com.slt.cardealership.domain.model.DealerInfo
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class InfoUiState {
    object Loading : InfoUiState()
    data class Success(val dealerInfo: DealerInfo) : InfoUiState()
    data class Error(val message: String) : InfoUiState()
}

@HiltViewModel
class InfoViewModel @Inject constructor(
    private val dealerRepository: DealerRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<InfoUiState>(InfoUiState.Loading)
    val uiState: StateFlow<InfoUiState> = _uiState.asStateFlow()

    init {
        fetchDealerInfo()
    }

    fun fetchDealerInfo() {
        viewModelScope.launch {
            _uiState.value = InfoUiState.Loading

            val dealerIdString = sessionManager.getDealerSlug()
            val dealerId = dealerIdString?.toLongOrNull()
            if (dealerId == null) {
                _uiState.value = InfoUiState.Error("Could not find saved Dealer ID.")
                return@launch
            }

            // --- THIS IS THE CORRECTED LOGIC ---
            // Call the repository function and handle the success or failure of the Result
            dealerRepository.getCombinedDealerInfo(dealerId)
                .onSuccess { combinedInfo ->
                    // This block runs only if the API calls were successful.
                    // 'combinedInfo' is the complete DealerInfo object.
                    _uiState.value = InfoUiState.Success(combinedInfo)
                }
                .onFailure { error ->
                    // This block runs if any of the API calls failed.
                    Log.e("InfoViewModel", "Failed to fetch dealer info", error)
                    _uiState.value = InfoUiState.Error(error.message ?: "An unknown error occurred")
                }
        }
    }
}
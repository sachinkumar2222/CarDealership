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
            try {
                // Get the saved dealer ID string from SessionManager
                val dealerIdString = sessionManager.getDealerSlug()

                if (dealerIdString.isNullOrBlank()) {
                    _uiState.value = InfoUiState.Error("Could not find saved Dealer ID.")
                    return@launch
                }

                // Convert the string to a Long
                val dealerId = dealerIdString.toLongOrNull()
                if (dealerId == null) {
                    _uiState.value = InfoUiState.Error("Saved Dealer ID is not a valid number.")
                    return@launch
                }

                // --- THIS IS THE CORRECTED LOGIC ---
                // Call the suspend function directly. If it fails, the catch block will run.
                val dealerInfo = dealerRepository.getDealerInfo(dealerId)

                // If the call succeeds, this line will be reached.
                _uiState.value = InfoUiState.Success(dealerInfo)

            } catch (e: Exception) {
                // If getDealerInfo fails, the exception is caught here.
                Log.e("InfoViewModel", "Failed to fetch dealer info", e)
                _uiState.value = InfoUiState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }
}
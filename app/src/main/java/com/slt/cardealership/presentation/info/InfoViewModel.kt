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
import org.json.JSONObject
import java.util.Base64
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

    private fun fetchDealerInfo() {
        viewModelScope.launch {
            _uiState.value = InfoUiState.Loading
            try {
                val token = sessionManager.authToken
                if (token == null) {
                    _uiState.value = InfoUiState.Error("User is not logged in.")
                    return@launch
                }

                val dealerId = getDealerIdFromToken(token)
                if (dealerId == null) {
                    _uiState.value = InfoUiState.Error("Could not find Dealer ID in token.")
                    return@launch
                }

                val dealerInfo = dealerRepository.getDealerInfo(dealerId)
                Log.d("infoviewmodel","${dealerInfo}")
                _uiState.value = InfoUiState.Success(dealerInfo)

            } catch (e: Exception) {
                Log.e("infoViewModel", "Failed to fetch dealer info", e)
                _uiState.value = InfoUiState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    private fun getDealerIdFromToken(token: String): String? {
        try {
            val parts = token.split(".")
            if (parts.size < 2) return null

            val payload = String(Base64.getUrlDecoder().decode(parts[1]))
            val json = JSONObject(payload)
            val fullDealerId = json.optString("extension_DealerId", null)
            Log.d("infoviewmodel","${fullDealerId}")
            return fullDealerId
        } catch (e: Exception) {
            // Log the exception in a real app
            return null
        }
    }
}

package com.slt.cardealership.presentation.mywebsites

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.data.local.SessionManager
import com.slt.cardealership.domain.model.DomainItem
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MyWebsitesUiState {
    object Loading : MyWebsitesUiState()
    data class Success(val domains: List<DomainItem>) : MyWebsitesUiState()
    data class Error(val message: String) : MyWebsitesUiState()
}

@HiltViewModel
class MyWebsitesViewModel @Inject constructor(
    private val dealerRepository: DealerRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<MyWebsitesUiState>(MyWebsitesUiState.Loading)
    val uiState: StateFlow<MyWebsitesUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "MyWebsitesViewModel"
    }

    init {
        fetchDomains()
    }

    fun fetchDomains() {
        viewModelScope.launch {
            _uiState.value = MyWebsitesUiState.Loading

            // 1. Fetch authoritative Dealer ID from API
            val dealerIdResult = dealerRepository.getUserAuthorization()
            val dealerId = dealerIdResult.getOrNull()?.dealerId

            if (dealerId == null) {
                // Fallback to session if API fails
                val sessionDealerId = sessionManager.getDealerId()?.toLong()
                if (sessionDealerId != null) {
                    Log.w(TAG, "getUserAuthorization failed, falling back to session ID: $sessionDealerId")
                    fetchDomainsWithId(sessionDealerId)
                } else {
                    _uiState.value = MyWebsitesUiState.Error("Failed to get Dealer ID from server or session.")
                }
                return@launch
            }

            Log.d(TAG, "Authoritative Dealer ID from API: $dealerId")
            fetchDomainsWithId(dealerId as Long)
        }
    }

    private suspend fun fetchDomainsWithId(dealerId: Long) {
        // Hardcoded pagination for now as per requirement
        dealerRepository.getDomains(page = 1, itemsPerPage = 10, dealerId = dealerId)
            .onSuccess { response ->
                _uiState.value = MyWebsitesUiState.Success(response.list)
            }
            .onFailure { error ->
                Log.e(TAG, "Failed to fetch domains", error)
                _uiState.value = MyWebsitesUiState.Error(error.message ?: "Failed to load domains")
            }
    }
}

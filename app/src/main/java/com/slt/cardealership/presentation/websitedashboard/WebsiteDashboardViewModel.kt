package com.slt.cardealership.presentation.websitedashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.domain.model.DomainItem
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class WebsiteDashboardUiState {
    object Loading : WebsiteDashboardUiState()
    data class Success(val domain: DomainItem) : WebsiteDashboardUiState()
    data class Error(val message: String) : WebsiteDashboardUiState()
}

@HiltViewModel
class WebsiteDashboardViewModel @Inject constructor(
    private val dealerRepository: DealerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WebsiteDashboardUiState>(WebsiteDashboardUiState.Loading)
    val uiState: StateFlow<WebsiteDashboardUiState> = _uiState.asStateFlow()

    fun fetchDomainDetails(domainId: Int) {
        viewModelScope.launch {
            _uiState.value = WebsiteDashboardUiState.Loading
            val result = dealerRepository.getDomainDetails(domainId)
            result.onSuccess { domain ->
                _uiState.value = WebsiteDashboardUiState.Success(domain)
            }.onFailure { error ->
                _uiState.value = WebsiteDashboardUiState.Error(error.message ?: "Failed to load domain details")
            }
        }
    }
}

package com.slt.cardealership.presentation.ManageClassified

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

@HiltViewModel
class ManageClassifiedDashboardViewModel @Inject constructor(
    private val repository: DealerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun fetchDomainDetails(domainId: Int) {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            repository.getDomainDetails(domainId)
                .onSuccess { domainItem ->
                    _uiState.value = DashboardUiState.Success(domainItem)
                }
                .onFailure { error ->
                    _uiState.value = DashboardUiState.Error(error.message ?: "Unknown error")
                }
        }
    }
}

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val domainItem: DomainItem) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

package com.slt.cardealership.presentation.websitedashboard.researchCompare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.domain.model.ResearchCompareItem
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class WebsiteResCompUiState {
    object Loading : WebsiteResCompUiState()
    data class Success(val items: List<ResearchCompareItem>) : WebsiteResCompUiState()
    data class Error(val message: String) : WebsiteResCompUiState()
}

@HiltViewModel
class WebsiteResCompViewModel @Inject constructor(
    private val repository: DealerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WebsiteResCompUiState>(WebsiteResCompUiState.Loading)
    val uiState: StateFlow<WebsiteResCompUiState> = _uiState.asStateFlow()

    fun fetchResearchCompares(domainId: Int) {
        viewModelScope.launch {
            _uiState.value = WebsiteResCompUiState.Loading
            val result = repository.getResearchCompareInterlinks(
                page = 1,
                itemsPerPage = 10, // Defaulting as per request
                domainId = domainId
            )
            result.onSuccess { items ->
                _uiState.value = WebsiteResCompUiState.Success(items)
            }.onFailure { error ->
                _uiState.value = WebsiteResCompUiState.Error(error.message ?: "Unknown error")
            }
        }
    }

    fun deleteResearchCompare(id: String, domainId: Int) {
        viewModelScope.launch {
            // Optimistic update or show loading?
            // For now, let's keep loading state simplistic or handling it quietly?
            // User requested: "after this delete the perticular entry or refetch the data"
            // Let's refetch.
            val result = repository.deleteResearchCompare(id)
            result.onSuccess {
                fetchResearchCompares(domainId)
            }.onFailure { error ->
                _uiState.value = WebsiteResCompUiState.Error(error.message ?: "Failed to delete")
            }
        }
    }
}

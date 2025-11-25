package com.slt.cardealership.presentation.leads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.data.local.SessionManager
import com.slt.cardealership.domain.model.InternetLead
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// UI State for the list
data class LeadsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val leads: List<InternetLead> = emptyList(),
    val totalCount: Int = 0,
    val currentPage: Int = 1,
    val canLoadMore: Boolean = false
)

@HiltViewModel
class InternetLeadsViewModel @Inject constructor(
    private val repository: DealerRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeadsUiState())
    val uiState: StateFlow<LeadsUiState> = _uiState.asStateFlow()

    // Holds the current API string (e.g., "sales-lead")
    private var currentApiType: String = ""
    private val itemsPerPage = 10

    /**
     * --- THIS IS THE KEY FUNCTION ---
     * It takes the UI Title (String) and converts it to the API Value.
     * This allows your UI to just pass "Sales Leads" or "Finance Leads".
     */
    fun loadLeadsByTitle(title: String) {
        val apiValue = when (title) {
            "General Leads" -> "sales-lead"
            "Sales Leads" -> "sales-lead" // Handle variants
            "Finance Leads" -> "finance-lead"
            "Service Leads" -> "service-lead"
            "Contact Leads" -> "contact-lead"
            "Inventory Leads" -> "vehicle-lead"
            "Cash for Car Leads" -> "cashforcar-lead"
            "Build & Price Leads" -> "buildandprice-lead"
            else -> "sales-lead" // Default fallback
        }

        // Now load the data using the API string
        loadLeads(apiValue)
    }

    private fun loadLeads(apiType: String, isRefresh: Boolean = false) {
        if (currentApiType != apiType) {
            _uiState.value = LeadsUiState(isLoading = true)
        }

        currentApiType = apiType
        val pageToLoad = if (isRefresh) 1 else _uiState.value.currentPage

        if (isRefresh) {
            _uiState.value = _uiState.value.copy(isLoading = true, leads = emptyList(), error = null)
        } else {
            if (_uiState.value.isLoading) return
            _uiState.value = _uiState.value.copy(isLoading = true)
        }

        viewModelScope.launch {
            val dealerId = sessionManager.getDealerId()
            if (dealerId == null) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Dealer ID not found")
                return@launch
            }

            repository.getInternetLeads(dealerId, currentApiType, pageToLoad, itemsPerPage)
                .onSuccess { response ->
                    val currentList = if (isRefresh) emptyList() else _uiState.value.leads
                    val newList = currentList + response.list

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        leads = newList,
                        totalCount = response.pagination.total,
                        currentPage = pageToLoad,
                        canLoadMore = newList.size < response.pagination.total
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load leads"
                    )
                }
        }
    }

    fun loadNextPage() {
        if (_uiState.value.canLoadMore && !_uiState.value.isLoading) {
            _uiState.value = _uiState.value.copy(currentPage = _uiState.value.currentPage + 1)
            loadLeads(currentApiType, isRefresh = false)
        }
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(currentPage = 1)
        loadLeads(currentApiType, isRefresh = true)
    }
}
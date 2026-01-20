package com.slt.cardealership.presentation.websitedashboard.menus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.domain.model.DomainMenu
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class WebsiteMenusUiState {
    object Loading : WebsiteMenusUiState()
    data class Success(val menus: List<DomainMenu>) : WebsiteMenusUiState()
    data class Error(val message: String) : WebsiteMenusUiState()
}

@HiltViewModel
class WebsiteMenusViewModel @Inject constructor(
    private val repository: DealerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WebsiteMenusUiState>(WebsiteMenusUiState.Loading)
    val uiState: StateFlow<WebsiteMenusUiState> = _uiState.asStateFlow()

    fun fetchMenus(domainId: Int) {
        viewModelScope.launch {
            _uiState.value = WebsiteMenusUiState.Loading
            try {
                // Hardcoded pagination for now as per requirement to just get list
                val result = repository.getDomainMenus(
                    page = 1,
                    itemsPerPage = 100, // Fetch enough to likely get all
                    domainId = domainId,
                    menuName = ""
                )
                result.fold(
                    onSuccess = { menus ->
                        _uiState.value = WebsiteMenusUiState.Success(menus)
                    },
                    onFailure = { error ->
                        _uiState.value = WebsiteMenusUiState.Error(error.message ?: "Failed to fetch menus")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = WebsiteMenusUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun deleteMenu(domainId: Int, menuId: String) {
        viewModelScope.launch {
            try {
                val result = repository.deleteDomainMenu(menuId)
                result.fold(
                    onSuccess = {
                        fetchMenus(domainId)
                    },
                    onFailure = { error ->
                        _uiState.value = WebsiteMenusUiState.Error(error.message ?: "Failed to delete menu")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = WebsiteMenusUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}

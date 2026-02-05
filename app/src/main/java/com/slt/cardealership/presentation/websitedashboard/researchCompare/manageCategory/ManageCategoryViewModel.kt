package com.slt.cardealership.presentation.websitedashboard.researchCompare.manageCategory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.domain.model.CreateResearchBlogCategoryRequest
import com.slt.cardealership.domain.model.ResearchBlogCategory
import com.slt.cardealership.domain.model.UpdateResearchBlogCategoryRequest
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ManageCategoryUiState {
    object Loading : ManageCategoryUiState()
    data class Success(val categories: List<ResearchBlogCategory>) : ManageCategoryUiState()
    data class Error(val message: String) : ManageCategoryUiState()
}

@HiltViewModel
class ManageCategoryViewModel @Inject constructor(
    private val repository: DealerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ManageCategoryUiState>(ManageCategoryUiState.Loading)
    val uiState: StateFlow<ManageCategoryUiState> = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow<ActionState>(ActionState.Idle)
    val actionState: StateFlow<ActionState> = _actionState.asStateFlow()

    sealed class ActionState {
        object Idle : ActionState()
        object Loading : ActionState()
        object Success : ActionState()
        data class Error(val message: String) : ActionState()
    }

    private val _domainNameState = MutableStateFlow<String>("")

    fun fetchCategories(domainId: Int) {
        viewModelScope.launch {
            // Fetch domain details if name is unknown
            if (_domainNameState.value.isEmpty()) {
                repository.getDomainDetails(domainId).onSuccess { domainItem ->
                    _domainNameState.value = domainItem.domainName ?: ""
                }
            }

            _uiState.value = ManageCategoryUiState.Loading
            val result = repository.getResearchBlogCategories(
                domainId = domainId,
                page = 1,
                itemsPerPage = 100, // Fetch ample amount
                domainName = _domainNameState.value
            )
            result.onSuccess { response ->
                _uiState.value = ManageCategoryUiState.Success(response.list)
            }.onFailure { error ->
                _uiState.value = ManageCategoryUiState.Error(error.message ?: "Failed to fetch categories")
            }
        }
    }

    fun createCategory(domainId: Int, domainName: String, name: String, description: String) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            // Use fetched domain name if parameter is empty
            val finalDomainName = if (domainName.isNotBlank()) domainName else _domainNameState.value

            val request = CreateResearchBlogCategoryRequest(
                domainId = domainId,
                domainName = finalDomainName,
                name = name,
                description = description
            )
            repository.createResearchBlogCategory(request).onSuccess {
                _actionState.value = ActionState.Success
                fetchCategories(domainId)
            }.onFailure {
                _actionState.value = ActionState.Error(it.message ?: "Failed to create category")
            }
        }
    }

    fun updateCategory(id: String, domainId: Int, domainName: String, name: String, description: String) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            // Use fetched domain name if parameter is empty
            val finalDomainName = if (domainName.isNotBlank()) domainName else _domainNameState.value

            val request = UpdateResearchBlogCategoryRequest(
                id = id,
                domainId = domainId,
                domainName = finalDomainName,
                name = name,
                description = description
            )
            repository.updateResearchBlogCategory(id, request).onSuccess {
                _actionState.value = ActionState.Success
                fetchCategories(domainId)
            }.onFailure {
                _actionState.value = ActionState.Error(it.message ?: "Failed to update category")
            }
        }
    }

    fun deleteCategory(id: String, domainId: Int) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            repository.deleteResearchBlogCategory(id).onSuccess {
                _actionState.value = ActionState.Success
                fetchCategories(domainId)
            }.onFailure {
                _actionState.value = ActionState.Error(it.message ?: "Failed to delete category")
            }
        }
    }

    fun resetActionState() {
        _actionState.value = ActionState.Idle
    }
}

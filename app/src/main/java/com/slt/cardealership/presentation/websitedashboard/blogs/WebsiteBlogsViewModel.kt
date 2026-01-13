package com.slt.cardealership.presentation.websitedashboard.blogs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.domain.model.DomainBlog
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class WebsiteBlogsUiState {
    object Loading : WebsiteBlogsUiState()
    data class Success(
        val blogs: List<DomainBlog>,
        val currentPage: Int,
        val totalPages: Int,
        val totalItems: Int,
        val itemsPerPage: Int
    ) : WebsiteBlogsUiState()
    data class Error(val message: String) : WebsiteBlogsUiState()
}

@HiltViewModel
class WebsiteBlogsViewModel @Inject constructor(
    private val dealerRepository: DealerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WebsiteBlogsUiState>(WebsiteBlogsUiState.Loading)
    val uiState: StateFlow<WebsiteBlogsUiState> = _uiState.asStateFlow()

    private var currentDomainId: Int = 0
    private var currentPage: Int = 1
    private val itemsPerPage: Int = 10

    fun fetchBlogs(domainId: Int, page: Int = 1) {
        currentDomainId = domainId
        currentPage = page
        
        viewModelScope.launch {
            _uiState.value = WebsiteBlogsUiState.Loading
            val result = dealerRepository.getDomainBlogs(domainId, page, itemsPerPage)
            result.onSuccess { response ->
                val totalPages = (response.pagination.total + itemsPerPage - 1) / itemsPerPage
                _uiState.value = WebsiteBlogsUiState.Success(
                    blogs = response.list,
                    currentPage = page,
                    totalPages = totalPages,
                    totalItems = response.pagination.total,
                    itemsPerPage = itemsPerPage
                )
            }.onFailure { error ->
                _uiState.value = WebsiteBlogsUiState.Error(error.message ?: "Failed to load blogs")
            }
        }
    }

    fun goToNextPage() {
        val state = _uiState.value
        if (state is WebsiteBlogsUiState.Success && currentPage < state.totalPages) {
            fetchBlogs(currentDomainId, currentPage + 1)
        }
    }

    fun goToPreviousPage() {
        if (currentPage > 1) {
            fetchBlogs(currentDomainId, currentPage - 1)
        }
    }

    fun goToPage(page: Int) {
        val state = _uiState.value
        if (state is WebsiteBlogsUiState.Success && page in 1..state.totalPages) {
            fetchBlogs(currentDomainId, page)
        }
    }
}

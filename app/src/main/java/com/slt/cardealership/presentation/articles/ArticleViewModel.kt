package com.slt.cardealership.presentation.articles

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.data.local.SessionManager
import com.slt.cardealership.domain.model.Post
import com.slt.cardealership.domain.repo.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class ArticleUiState {
    object Loading : ArticleUiState()
    data class Success(val articles: List<Post>) : ArticleUiState()
    data class Error(val message: String) : ArticleUiState()
}

@HiltViewModel
class ArticleViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow<ArticleUiState>(ArticleUiState.Loading)
    val uiState = _uiState.asStateFlow()

    // Sort State
    var sortBy by mutableStateOf("CreatedOn")
    var sortOrder by mutableStateOf("desc")

    init {
        fetchPosts()
    }

    fun fetchPosts() {
        viewModelScope.launch {
            _uiState.value = ArticleUiState.Loading
            val dealerId = sessionManager.getDealerSlug()?.toLongOrNull()
            if (dealerId == null) {
                _uiState.value = ArticleUiState.Error("Dealer ID not found.")
                return@launch
            }
            postRepository.getPosts(dealerId, page = 1, orderBy = sortBy, order = sortOrder)
                .onSuccess { posts -> _uiState.value = ArticleUiState.Success(posts) }
                .onFailure { _uiState.value = ArticleUiState.Error(it.message ?: "Failed to load posts.") }
        }
    }

    fun updateSort(newSortBy: String, newSortOrder: String) {
        sortBy = newSortBy
        sortOrder = newSortOrder
        fetchPosts()
    }

    fun deleteArticle(articleId: String) {
        viewModelScope.launch {
            val dealerId = sessionManager.getDealerSlug()?.toLongOrNull() ?: return@launch
            postRepository.deletePost(dealerId, articleId)
                .onSuccess { fetchPosts() } // Refresh list
                .onFailure { /* Handle error */ }
        }
    }
}
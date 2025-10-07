package com.slt.cardealership.presentation.articles

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

    init {
        fetchArticles()
    }

    fun fetchArticles() {
        viewModelScope.launch {
            _uiState.value = ArticleUiState.Loading
            val dealerId = sessionManager.getDealerSlug()?.toLongOrNull()
            if (dealerId == null) {
                _uiState.value = ArticleUiState.Error("Dealer ID not found.")
                return@launch
            }
            postRepository.getPosts(dealerId)
                .onSuccess { posts -> _uiState.value = ArticleUiState.Success(posts) }
                .onFailure { _uiState.value = ArticleUiState.Error(it.message ?: "Failed to load.") }
        }
    }

    fun deleteArticle(postId: String) {
        viewModelScope.launch {
            val dealerId = sessionManager.getDealerSlug()?.toLongOrNull() ?: return@launch
            postRepository.deletePost(dealerId, postId)
                .onSuccess { fetchArticles() } // Refresh the list on success
                .onFailure { /* Optionally show a toast or error message */ }
        }
    }
}
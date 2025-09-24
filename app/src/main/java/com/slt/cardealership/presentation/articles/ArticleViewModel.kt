package com.slt.cardealership.presentation.articles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.domain.model.Article
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Sealed class to represent the different states of the UI
sealed class ArticleUiState {
    object Loading : ArticleUiState()
    data class Success(val articles: List<Article>) : ArticleUiState()
    data class Error(val message: String) : ArticleUiState()
}

@HiltViewModel
class ArticleViewModel @Inject constructor(
    // NOTE: You would inject your repository or use cases here
    // private val articleRepository: ArticleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ArticleUiState>(ArticleUiState.Loading)
    val uiState: StateFlow<ArticleUiState> = _uiState.asStateFlow()

    init {
        fetchArticles()
    }

    fun fetchArticles() {
        viewModelScope.launch {
            _uiState.value = ArticleUiState.Loading
            try {
                val mockArticles = listOf(
                    Article("1", "The Future of Electric Vehicles", "All Domain", "Published", "2025-09-24"),
                    Article("2", "Understanding Hybrid Technology", "All Domain", "Draft", "2025-09-23")
                )
                _uiState.value = ArticleUiState.Success(mockArticles)

            } catch (e: Exception) {
                _uiState.value = ArticleUiState.Error(e.message ?: "Failed to load articles")
            }
        }
    }

    fun deleteArticle(articleId: String) {
        // TODO: Implement the logic to call the repository to delete an article
    }
}

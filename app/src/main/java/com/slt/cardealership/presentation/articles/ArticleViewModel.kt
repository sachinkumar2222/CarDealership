package com.slt.cardealership.presentation.articles

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auth0.android.jwt.JWT
import com.slt.cardealership.data.local.SessionManager
import com.slt.cardealership.domain.model.Article
import com.slt.cardealership.domain.repo.ArticleRepository
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
    private val articleRepository: ArticleRepository,
    private val sessionManager: SessionManager // Inject SessionManager to get the auth token
) : ViewModel() {

    private val _uiState = MutableStateFlow<ArticleUiState>(ArticleUiState.Loading)
    val uiState: StateFlow<ArticleUiState> = _uiState.asStateFlow()

    // A local cache of the article list for optimistic UI updates (e.g., on delete)
    private var currentArticleList: List<Article> = listOf()

    private var allArticles: List<Article> = emptyList()

    // --- NEW: State for the current filter values ---
    var articleNameFilter by mutableStateOf("")
        private set
    var statusFilter by mutableStateOf("All") // Default to "All"
        private set

    init {
        fetchArticles()
    }

    fun fetchArticles() {
        viewModelScope.launch {
            _uiState.value = ArticleUiState.Loading

            val token = sessionManager.authToken
            if (token == null) {
                _uiState.value = ArticleUiState.Error("Authentication token not found.")
                return@launch
            }

            // The API requires the full dealerId, including the prefix
            val dealerId = getDealerIdFromToken(token)
            if (dealerId == null) {
                _uiState.value = ArticleUiState.Error("Could not find Dealer ID in token.")
                return@launch
            }

            // --- THIS IS THE LIVE API CALL ---
            // It calls the repository to get the live data from the API
            articleRepository.getArticles(dealerId).fold(
                onSuccess = { articles ->
                    currentArticleList = articles
                    _uiState.value = ArticleUiState.Success(articles)
                },
                onFailure = { error ->
                    _uiState.value = ArticleUiState.Error(error.message ?: "An unknown error occurred")
                }
            )
        }
    }

    fun deleteArticle(articleId: String) {
        viewModelScope.launch {
            articleRepository.deleteArticle(articleId).onSuccess {
                // On successful deletion, update the UI immediately by removing
                // the item from our local list, avoiding a full network refresh.
                val updatedList = currentArticleList.filterNot { it.id == articleId }
                currentArticleList = updatedList
                _uiState.value = ArticleUiState.Success(updatedList)
            }
            // In a real app, you would also handle the onFailure case here
            // to show an error message to the user (e.g., with a snackbar).
        }
    }

    fun applyFilters(name: String, status: String) {
        articleNameFilter = name
        statusFilter = status

        val filteredList = allArticles.filter { article ->
            val nameMatches = if (name.isNotBlank()) {
                article.title.contains(name, ignoreCase = true)
            } else {
                true // No name filter, so it's a match
            }

            val statusMatches = if (status != "All") {
                article.status.equals(status, ignoreCase = true)
            } else {
                true // "All" status, so it's a match
            }

            nameMatches && statusMatches
        }
        _uiState.value = ArticleUiState.Success(filteredList)
    }

    fun resetFilters() {
        // Clear local filter state
        articleNameFilter = ""
        statusFilter = "All"
        // Reset the UI to show the original, full list
        _uiState.value = ArticleUiState.Success(allArticles)
    }

    /**
     * Parses the JWT token to extract the full "extension_DealerId" claim.
     */
    private fun getDealerIdFromToken(token: String): String? {
        return try {
            val jwt = JWT(token)
            jwt.getClaim("extension_DealerId").asString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}


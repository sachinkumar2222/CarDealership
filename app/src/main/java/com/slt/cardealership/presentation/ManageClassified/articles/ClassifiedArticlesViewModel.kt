package com.slt.cardealership.presentation.ManageClassified.articles

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.data.local.SessionManager
import com.slt.cardealership.domain.model.ArticleLink
import com.slt.cardealership.domain.model.Post
import com.slt.cardealership.domain.repo.DealerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ArticlesUiState {
    data object Loading : ArticlesUiState
    data class Success(
        val posts: List<Post>,
        val articleLinks: List<ArticleLink>,
        val domainName: String
    ) : ArticlesUiState
    data class Error(val message: String) : ArticlesUiState
}

@HiltViewModel
class ClassifiedArticlesViewModel @Inject constructor(
    private val repository: DealerRepository,
    private val postRepository: com.slt.cardealership.domain.repo.PostRepository,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val siteId: String? = savedStateHandle["siteId"]

    private val _uiState = MutableStateFlow<ArticlesUiState>(ArticlesUiState.Loading)
    val uiState: StateFlow<ArticlesUiState> = _uiState.asStateFlow()

    private var dealerId: Long? = null
    private var domainName: String? = null

    private var sortOrder = "desc"
    private var sortBy = "CreatedOn"

    init {
        fetchData()
    }

    fun fetchData() {
        val id = siteId?.toIntOrNull()
        if (id == null) {
            _uiState.update { ArticlesUiState.Error("Invalid Site ID") }
            return
        }

        viewModelScope.launch {
            _uiState.update { ArticlesUiState.Loading }

            val domainResult = repository.getDomainDetails(id)

            domainResult.onSuccess { domainItem ->
                domainName = domainItem.domainName

                if (domainItem.dealerId != null) {
                    dealerId = domainItem.dealerId.toLong()
                } else {
                    dealerId = sessionManager.getDealerId()?.toLong()
                }

                val currentDealerId = dealerId
                if (currentDealerId != null) {
                    val postsDeferred = async {
                        repository.getClassifiedPosts(
                            dealerId = currentDealerId,
                            page = 1,
                            itemsPerPage = 20,
                            orderBy = sortBy,
                            order = sortOrder,
                            status = "",
                            name = "",
                            haveContent = "",
                            haveLinks = "",
                            haveImage = "",
                            domainName = domainName
                        )
                    }
                    val linksDeferred = async { repository.getArticleLinks(currentDealerId) }

                    val postsResult = postsDeferred.await()
                    val linksResult = linksDeferred.await()

                    if (postsResult.isSuccess && linksResult.isSuccess) {
                        _uiState.update {
                            ArticlesUiState.Success(
                                posts = postsResult.getOrDefault(emptyList()),
                                articleLinks = linksResult.getOrDefault(emptyList()),
                                domainName = domainItem.domainName
                            )
                        }
                    } else {
                        val message = postsResult.exceptionOrNull()?.message
                            ?: linksResult.exceptionOrNull()?.message
                            ?: "Failed to fetch data"
                        _uiState.update { ArticlesUiState.Error(message) }
                    }
                } else {
                    _uiState.update { ArticlesUiState.Error("Dealer ID not found") }
                }
            }.onFailure { exception: Throwable ->
                _uiState.update { ArticlesUiState.Error("Failed to fetch domain details: ${exception.localizedMessage ?: "Unknown error"}") }
            }
        }
    }

    fun onSortChange(newSortBy: String, newSortOrder: String) {
        if (sortBy != newSortBy || sortOrder != newSortOrder) {
            sortBy = newSortBy
            sortOrder = newSortOrder
            fetchData()
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            val currentDealerId = dealerId
            if (currentDealerId == null) {
                // If dealerId is not available, just refresh to be safe
                fetchData()
                return@launch
            }

            // Optimistically remove from UI
            val currentState = _uiState.value
            if (currentState is ArticlesUiState.Success) {
                _uiState.update {
                    ArticlesUiState.Success(
                        posts = currentState.posts.filterNot { it.id == postId },
                        articleLinks = currentState.articleLinks,
                        domainName = currentState.domainName
                    )
                }
            }

            // Call API
            postRepository.deletePost(currentDealerId, postId)
                .onSuccess {
                    // Success - item already removed from UI
                }
                .onFailure {
                    // On failure, refresh to restore accurate state
                    fetchData()
                }
        }
    }
}

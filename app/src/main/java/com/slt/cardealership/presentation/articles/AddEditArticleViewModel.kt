//package com.slt.cardealership.presentation.articles
//
//import android.net.Uri
//import android.util.Log
//import androidx.compose.runtime.*
//import androidx.lifecycle.SavedStateHandle
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.auth0.android.jwt.JWT
//import com.slt.cardealership.data.local.SessionManager
//import com.slt.cardealership.domain.model.Article
//import com.slt.cardealership.domain.repo.ArticleRepository
//import com.slt.cardealership.utils.TokenParser
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//@HiltViewModel
//class AddEditArticleViewModel @Inject constructor(
//    private val articleRepository: ArticleRepository,
//    private val sessionManager: SessionManager,
//    private val savedStateHandle: SavedStateHandle
//) : ViewModel() {
//
//    // Get the articleId from navigation arguments. It will be null if this is a new article.
//    private val articleId: String? = savedStateHandle["articleId"]
//
//    // Form state variables
//    var articleTitle by mutableStateOf("")
//    var slug by mutableStateOf("")
//    var status by mutableStateOf("Draft")
//    var domain by mutableStateOf("All Domains")
//    var metaTitle by mutableStateOf("")
//    var metaDescription by mutableStateOf("")
//    var articleContent by mutableStateOf("")
//    var featuredImageUri by mutableStateOf<Uri?>(null)
//
//    // A state to communicate the result of the save operation back to the UI
//    private val _saveResult = MutableStateFlow<Result<Unit>?>(null)
//    val saveResult: StateFlow<Result<Unit>?> = _saveResult.asStateFlow()
//
//    init {
//        if (articleId != null) {
//            // This is where you would load the existing article's data.
//            // For now, we are focusing on the save functionality.
//            println("Editing article with ID: $articleId")
//            loadArticleDetails(articleId)
//        }
//    }
//
//    private fun loadArticleDetails(id: String) {
//        viewModelScope.launch {
//            val token = sessionManager.authToken
//            val dealerId = getDealerIdFromToken(token!!)
//
//            if (dealerId == null) {
//                // Handle error: couldn't get dealerId
//                return@launch
//            }
//
//            // Fetch the full list of articles to find the one we need
//            articleRepository.getArticles(dealerId)
//                .onSuccess { articles ->
//                    val articleToEdit = articles.find { it.id == id }
//                    if (articleToEdit != null) {
//                        // Populate the form fields with the loaded data
//                        articleTitle = articleToEdit.title
//                        status = articleToEdit.status
//                        domain = articleToEdit.domainName
//                        // Note: Your Article model from the API doesn't have slug, meta fields yet.
//                        // You will need to add them to the Article.kt data class to populate them here.
//                    }
//                }
//                .onFailure {
//                    Log.d("editviewmodel","failed")
//                }
//        }
//    }
//
//    fun onSaveArticle() {
//        viewModelScope.launch {
//            // Construct the article object from the current form state
//            val articleToSave = Article(
//                id = articleId ?: "", // Use existing ID for update, or empty for new
//                title = articleTitle,
//                domainName = domain,
//                status = status,
//                createdOn = ""
//            )
//
//            // Decide whether to call the add or update function based on articleId
//            val result = if (articleId == null) {
//                articleRepository.addArticle(articleToSave)
//            } else {
//                articleRepository.updateArticle(articleId, articleToSave)
//            }
//
//            Log.d("addarticleviewmodel","article added")
//
//            // Post the result to the UI
//            result.onSuccess {
//                _saveResult.value = Result.success(Unit)
//                Log.d("addarticleviewmodel","article added")
//            }.onFailure {
//                _saveResult.value = Result.failure(it)
//            }
//        }
//    }
//    private fun getDealerIdFromToken(token: String): String? {
//        return try {
//            val jwt = JWT(token)
//            jwt.getClaim("extension_DealerId").asString()
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }
//}

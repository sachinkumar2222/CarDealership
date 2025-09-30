//package com.slt.cardealership.data.repo
//
//import android.util.Log
//import com.slt.cardealership.data.remote.network.ApiService
//import com.slt.cardealership.domain.model.Article
//import com.slt.cardealership.domain.repo.ArticleRepository
//import javax.inject.Inject
//
//
//class ArticleRepositoryImpl @Inject constructor(
//    private val apiService: ApiService
//) : ArticleRepository {
//
//    override suspend fun getArticles(dealerId: String): Result<List<Article>> {
//        return try {
//            val responseWrapper = apiService.getArticles(dealerId)
//            Log.d("articles","${responseWrapper}")
//            val articles = responseWrapper.articles ?: emptyList()
//            Result.success(articles)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    override suspend fun addArticle(article: Article): Result<Article> {
//        return try {
//            val newArticle = apiService.addArticle(article)
//            Result.success(newArticle)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    override suspend fun updateArticle(articleId: String, article: Article): Result<Article> {
//        return try {
//            val updatedArticle = apiService.updateArticle(articleId, article)
//            Result.success(updatedArticle)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//    override suspend fun deleteArticle(articleId: String): Result<Unit> {
//        return try {
//            apiService.deleteArticle(articleId)
//            Result.success(Unit)
//        } catch (e: Exception) {
//            Log.e("ArticleRepo", "Failed to delete article", e)
//            Result.failure(e)
//        }
//    }
//}

package com.slt.cardealership.data.repo

import com.slt.cardealership.data.remote.network.ApiService
import com.slt.cardealership.domain.model.Article
import com.slt.cardealership.domain.repo.ArticleRepository
import javax.inject.Inject

/**
 * Implementation of the ArticleRepository that uses a remote data source (Retrofit).
 */
class ArticleRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ArticleRepository {

    override suspend fun addArticle(article: Article): Result<Article> {
        return try {
            val newArticle = apiService.addArticle(article)
            Result.success(newArticle)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateArticle(articleId: String, article: Article): Result<Article> {
        return try {
            val updatedArticle = apiService.updateArticle(articleId, article)
            Result.success(updatedArticle)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

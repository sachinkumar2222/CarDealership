package com.slt.cardealership.domain.repo

import com.slt.cardealership.domain.model.Article

interface ArticleRepository {
    suspend fun addArticle(article: Article): Result<Article>
    suspend fun updateArticle(articleId: String, article: Article): Result<Article>
}
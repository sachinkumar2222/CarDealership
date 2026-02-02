package com.slt.cardealership.domain.repo

import com.slt.cardealership.domain.model.Post
import com.slt.cardealership.domain.model.PostImageUploadResponse
import com.slt.cardealership.domain.model.ArticleLink
import com.slt.cardealership.domain.model.SeoTag
import java.io.File

interface PostRepository {
    suspend fun getPostById(dealerId: Long, postId: String): Result<Post>
    suspend fun getPosts(
        dealerId: Long,
        page: Int = 1,
        orderBy: String? = null,
        order: String? = null
    ): Result<List<Post>>
    suspend fun deletePost(dealerId: Long, postId: String): Result<Unit>
    suspend fun addPost(dealerId: Long, post: Post): Result<Unit>
    suspend fun updatePost(dealerId: Long, postId: String, post: Post): Result<Unit>
    suspend fun uploadPostImage(dealerId: Long, imageFile: File): Result<String>

    // Article Links
    suspend fun getArticleLinks(dealerId: Long): Result<List<ArticleLink>>
    suspend fun addArticleLink(dealerId: Long, link: String, type: String): Result<Unit>
    suspend fun deleteArticleLink(dealerId: Long, linkId: String): Result<Unit>

    // SEO Tags
    suspend fun getDealerSeoTags(dealerId: Long): Result<List<SeoTag>>
}
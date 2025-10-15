package com.slt.cardealership.domain.repo

import com.slt.cardealership.domain.model.Post
import com.slt.cardealership.domain.model.PostImageUploadResponse
import java.io.File

interface PostRepository {
    suspend fun getPostById(dealerId: Long, postId: String): Result<Post>
    suspend fun getPosts(dealerId: Long): Result<List<Post>>
    suspend fun deletePost(dealerId: Long, postId: String): Result<Unit>
    suspend fun addPost(dealerId: Long, post: Post): Result<Unit>
    suspend fun updatePost(dealerId: Long, postId: String, post: Post): Result<Unit>
    suspend fun uploadPostImage(dealerId: Long, imageFile: File): Result<String>
}
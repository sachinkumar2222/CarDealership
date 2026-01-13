package com.slt.cardealership.data.repo

import android.util.Log
import com.slt.cardealership.data.remote.network.ApiService
import com.slt.cardealership.domain.model.Post
import com.slt.cardealership.domain.repo.PostRepository
import java.io.File
import javax.inject.Inject
import com.slt.cardealership.domain.model.PostImageUploadResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response

class PostRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : PostRepository {
    override suspend fun getPosts(dealerId: Long): Result<List<Post>> {
        return try {
            val response = apiService.getPosts(dealerId)
            Result.success(response.list ?: emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePost(dealerId: Long, postId: String): Result<Unit> {
        return try {
            apiService.deletePost(dealerId, postId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPostById(dealerId: Long, postId: String): Result<Post> {
        return try {
            Result.success(apiService.getPostById(dealerId, postId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addPost(dealerId: Long, post: Post): Result<Unit> {
        return try {
            val response = apiService.addPost(dealerId, post)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to add post. Code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePost(dealerId: Long, postId: String, post: Post): Result<Unit> {
        return try {
            val response = apiService.updatePost(dealerId, postId, post)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update post. Code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadPostImage(dealerId: Long, imageFile: File): Result<String> {
        Log.d("ImageUpload", "Uploading image: ${imageFile.name}")
        return try {
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)
            val imageUrl = apiService.uploadPostImage(dealerId, imagePart)
            Log.d("ImageUpload", "Image part created")
            Result.success(imageUrl)
        } catch (e: Exception) {
            Log.e("ImageUpload", "Error uploading image", e)
            Result.failure(e)
        }
    }
}
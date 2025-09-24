package com.slt.cardealership.data.remote.network

import com.slt.cardealership.domain.model.Article
import com.slt.cardealership.domain.model.DealerInfo
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("Dealers/{dealerId}")
    suspend fun getDealerInfo(
        @Path("dealerId") dealerId: String
    ): DealerInfo

    @GET("DealerPosts")
    suspend fun getArticles(
        @Query("dealerId") dealerId: String
    ): List<Article> // Assuming the API returns a list of articles

    @POST("DealerPosts")
    suspend fun addArticle(
        @Body article: Article // You'll need a request body data class for this
    ): Article // Assuming the API returns the created article

    @POST("DealerPosts/{postId}")
    suspend fun updateArticle(
        @Path("postId") postId: String,
        @Body article: Article
    ): Article

    @DELETE("DealerPosts/{postId}")
    suspend fun deleteArticle(
        @Path("postId") postId: String
    ): Unit // Assuming the API returns an empty response on success

}

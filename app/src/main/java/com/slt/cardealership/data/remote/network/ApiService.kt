package com.slt.cardealership.data.remote.network

import com.slt.cardealership.domain.model.Banner
import com.slt.cardealership.domain.model.BannerListResponse
import com.slt.cardealership.domain.model.DealerDetailsResponse
import com.slt.cardealership.domain.model.DealerHours
import com.slt.cardealership.domain.model.DealerMetasResponse
import com.slt.cardealership.domain.model.GalleryListResponse
import com.slt.cardealership.domain.model.GalleryResponseObject
import com.slt.cardealership.domain.model.Post
import com.slt.cardealership.domain.model.PostListResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    @GET("dealer-api/Dealers/{dealerId}")
    suspend fun getDealerDetails(@Path("dealerId") dealerId: Long): DealerDetailsResponse


    @GET("dealer-api/dealer-metas/{dealerId}")
    suspend fun getDealerMetas(
        @Path("dealerId") dealerId: Long
    ): DealerMetasResponse

    @GET("dealer-api/Dealers/{dealerId}/Hours")
    suspend fun getDealerHours(@Path("dealerId") dealerId: Long): List<DealerHours>

    @GET("dealer-api/dealers/{dealerId}/posts")
    suspend fun getPosts(
        @Path("dealerId") dealerId: Long
    ): PostListResponse

    @GET("dealer-api/dealers/{dealerId}/posts/{postId}")
    suspend fun getPostById(
        @Path("dealerId") dealerId: Long,
        @Path("postId") postId: String
    ): Post

    @Multipart
    @POST("dealer-api/dealers/{dealerId}/banners")
    suspend fun addBanner(
        @Path("dealerId") dealerId: Long,
        @Part("title") title: RequestBody,
        @Part("url") url: RequestBody,
        @Part("start_date") startDate: RequestBody,
        @Part image: MultipartBody.Part,
        // The API requires these fields, we can send default/current values
        @Part("created_by") createdBy: RequestBody,
        @Part("updated_by") updatedBy: RequestBody,
        @Part("domain_id") domainId: RequestBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
    ): Response<Unit>


    @POST("dealer-api/dealers/{dealerId}/posts")
    suspend fun addPost(@Path("dealerId") dealerId: Long, @Body post: Post): Post

    @PUT("dealer-api/dealers/{dealerId}/posts/{postId}")
    suspend fun updatePost(
        @Path("dealerId") dealerId: Long,
        @Path("postId") postId: String,
        @Body post: Post
    ): Post

    @DELETE("dealer-api/dealers/{dealerId}/posts/{postId}")
    suspend fun deletePost(
        @Path("dealerId") dealerId: Long,
        @Path("postId") postId: String
    ): Response<Unit> // Use Response<Unit> for empty responses

    @Multipart
    @POST("dealer-api/dealers/{dealerId}/posts/UploadImage")
    suspend fun uploadPostImage(
        @Path("dealerId") dealerId: Long,
        @Part image: MultipartBody.Part
    ): String

    @GET("dealer-api/dealers/{dealerId}/banners")
    suspend fun getBanners(@Path("dealerId") dealerId: Long): BannerListResponse

    @Multipart
    @PUT("dealer-api/dealers/{dealerId}/banners/{bannerId}")
    suspend fun updateBanner(
        @Path("dealerId") dealerId: Long,
        @Path("bannerId") bannerId: String,
        @Part("title") title: RequestBody,
        @Part("url") url: RequestBody,
        @Part("start_date") startDate: RequestBody,
        @Part image: MultipartBody.Part?, // Image is optional on update
        @Part("updated_by") updatedBy: RequestBody,
        @Part("domain_id") domainId: RequestBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
    ): Response<Unit>

    @GET("dealer-api/dealers/{dealerId}/banners/{bannerId}")
    suspend fun getBannerDetails(
        @Path("dealerId") dealerId: Long,
        @Path("bannerId") bannerId: String
    ): Banner

    @GET("dealer-api/dealers/{dealerId}/Gallery")
    suspend fun getGalleryImages(@Path("dealerId") dealerId: Long): GalleryResponseObject

    @Multipart
    @POST("dealer-api/dealers/{dealerId}/Gallery")
    suspend fun addGalleryImage(
        @Path("dealerId") dealerId: Long,
        @Part image: MultipartBody.Part
    ): Response<Unit>

}

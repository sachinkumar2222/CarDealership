package com.slt.cardealership.domain.model

import com.google.gson.annotations.SerializedName

data class PostListResponse(
    val list: List<Post>?
)

data class Post(
    val id: String?,
    @SerializedName("dealer_id") val dealerId: Long?,
    val name: String?,
    val slug: String?,
    val status: String?,
    @SerializedName("meta_title") val metaTitle: String?,
    @SerializedName("meta_description") val metaDescription: String?,
    val image: String?,
    val content: String?,
    @SerializedName("created_on") val createdOn: Long?
)

// We'll use this for uploading a post image
data class PostImageUploadResponse(
    val path: String
)
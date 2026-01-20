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
    @SerializedName("created_on") val createdOn: Long?,
    @SerializedName("updated_on") val updatedOn: Long? = null,
    @SerializedName("created_by") val createdBy: String? = null,
    @SerializedName("updated_by") val updatedBy: String? = null,
    @SerializedName("domain_name") val domainName: String? = null,

    // Flags from payload
    @SerializedName("have_content") val haveContent: Boolean? = null,
    @SerializedName("have_image") val haveImage: Boolean? = null,
    @SerializedName("have_links") val haveLinks: Boolean? = null,

    val tags: List<SeoTag>? = null,
    val ctas: List<PostCta>? = null
)

data class PostCta(
    val label: String,
    val url: String
)

// We'll use this for uploading a post image
data class PostImageUploadResponse(
    val path: String
)
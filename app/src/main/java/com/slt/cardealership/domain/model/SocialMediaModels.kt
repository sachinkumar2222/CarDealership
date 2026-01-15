package com.slt.cardealership.domain.model

import com.google.gson.annotations.SerializedName

data class SocialMediaListResponse(
    @SerializedName("pagination") val pagination: Pagination,
    @SerializedName("list") val list: List<SocialMediaItem>
)

data class SocialMediaItem(
    @SerializedName("id") val id: Int,
    @SerializedName("url") val url: String?,
    @SerializedName("media_type") val mediaType: String,
    @SerializedName("domain_name") val domainName: String? = null,
    @SerializedName("domain_id") val domainId: Int? = null
)

data class SaveSocialMediaRequest(
    @SerializedName("domain_id") val domainId: Int,
    @SerializedName("url") val url: String,
    @SerializedName("media_type") val mediaType: String,
    @SerializedName("domain") val domain: String? = null,
    @SerializedName("getSocialMedia") val getSocialMedia: Int = 5
)

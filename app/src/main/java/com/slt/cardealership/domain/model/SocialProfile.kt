package com.slt.cardealership.domain.model

import com.google.gson.annotations.SerializedName

data class SocialProfileItem(
    @SerializedName("social_type")
    val socialType: String,
    @SerializedName("social_link")
    val socialLink: String
)

data class SocialProfileRequest(
    @SerializedName("id")
    val id: Long, // Dealer ID
    @SerializedName("social")
    val social: List<SocialProfileItem>,
    @SerializedName("created_by")
    val createdBy: Long,
    @SerializedName("created_on")
    val createdOn: Long
)

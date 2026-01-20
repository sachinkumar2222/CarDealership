package com.slt.cardealership.domain.model

import com.google.gson.annotations.SerializedName

/**
 * This class matches the API response: {"pagination":..., "list":[...]}
 */
data class SeoTagListResponse(
    val pagination: SeoPagination,
    val list: List<SeoTag>
)

data class SeoPagination(
    val page: Int,
    val total: Int
)

/**
 * This is the CORRECT SeoTag model, matching the API JSON
 * Note: id is a String (UUID), not an Int
 */
data class SeoTag(
    val id: String? = null,
    val dealer_id: Int? = null,
    @SerializedName("tag_name")
    val tagName: String,
    @SerializedName("tag_url")
    val tagUrl: String
)

/**
 * This is the request to create a new tag.
 * FIX: It now includes the dealer_id.
 */
data class AddSeoTagRequest(
    @SerializedName("tag_name")
    val tagName: String,
    @SerializedName("tag_url")
    val tagUrl: String,
    @SerializedName("dealer_id") // <-- This was missing
    val dealerId: Long
)

/**
 * This model is still needed for mapping.
 * FIX: It now uses a List<String> for the UUIDs.
 */
data class MapSeoTagsRequest(
    @SerializedName("tag_ids")
    val tagIds: List<String>
)
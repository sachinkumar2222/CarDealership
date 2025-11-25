package com.slt.cardealership.domain.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Represents a single SEO Category from the API.
 * Used to populate the dropdown list.
 * API: GET /systems-api/dealer-seo-category/getall
 */
@Parcelize
data class SeoCategory(
    val id: Int,
    val name: String
) : Parcelable

/**
 * Represents a single SEO Menu item returned from the server.
 * API: GET /dealer-api/dealers/{dealer_id}/Seomenus
 */
@Parcelize
data class SeoMenu(
    val id: String, // This is the UUID
    @SerializedName("dealer_id")
    val dealerId: Int,
    @SerializedName("seo_category_id")
    val seoCategoryId: Int,
    @SerializedName("menu_label")
    val menuLabel: String,
    @SerializedName("menu_url")
    val menuUrl: String,
    @SerializedName("menu_target")
    val menuTarget: String
) : Parcelable

/**
 * This is the wrapper object we must send for the POST request.
 * API: POST /dealer-api/dealers/{dealer_id}/SeoMenus
 */
data class SeoMenuRequest(
    val list: List<SeoMenuPayload>
)

/**
 * This is the object for a single menu item in the POST request.
 * Notice it does not have the 'id' or 'dealer_id'.
 */
data class SeoMenuPayload(
    val seo_category_id: Int,
    val menu_label: String,
    val menu_target: String,
    val menu_url: String
)
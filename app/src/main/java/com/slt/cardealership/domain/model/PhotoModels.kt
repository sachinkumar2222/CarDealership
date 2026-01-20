package com.slt.cardealership.domain.model

import com.google.gson.annotations.SerializedName

data class BannerListResponse(
    val pagination: Pagination?,
    val list: List<Banner>
)

data class Banner(
    val id: String?,
    val title: String?,
    val url: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("domain_name") val domainName: String?,
    @SerializedName("domain_id") val domainId: Int?,
    @SerializedName("dealer_id") val dealerId: Int?,
    @SerializedName("start_date") val startDate: Long?,
    @SerializedName("end_date") val endDate: Long?,
    @SerializedName("created_by") val createdBy: String?,
    @SerializedName("updated_by") val updatedBy: String?,
    @SerializedName("created_on") val createdOn: Long?,
    @SerializedName("updated_on") val updatedOn: Long?,
    // These fields are returned as null in API but included in response
    val domain: Any? = null,
    val dealer: Any? = null,
    val createdByUser: Any? = null,
    val updatedByUser: Any? = null
)

data class GalleryListResponse(
    val list: List<GalleryResponseObject>
)

// This class for the object inside the list is still correct
data class GalleryResponseObject(
    val id: String?,
    val images: List<String>?
)

data class GalleryImage(
    val id: String?,
    val imageUrl: String?
)
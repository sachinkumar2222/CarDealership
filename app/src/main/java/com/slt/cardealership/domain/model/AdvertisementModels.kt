package com.slt.cardealership.domain.model

import com.google.gson.annotations.SerializedName

/**
 * Wraps the list response for Advertisements, including pagination.
 */
data class AdvertisementListResponse(
    @SerializedName("pagination")
    val pagination: Pagination?,
    @SerializedName("list")
    val list: List<Advertisement>?
)

/**
 * Represents a single Advertisement.
 * This is a "super-set" model to handle GET list, GET details, and POST/PUT.
 * All fields that are not in all responses are nullable.
 */
data class Advertisement(
    @SerializedName("id")
    val id: String?,
    @SerializedName("title")
    val title: String, // Assuming title is always required
    @SerializedName("type")
    val type: String?, // "General", "Co-op", "Dealership Ad"
    @SerializedName("start_date")
    val startDate: Long?,
    @SerializedName("end_date")
    val endDate: Long?,
    @SerializedName("condition")
    val condition: String?, // "New", "Used", "CPO"

    // --- Fields from GET list ---
    @SerializedName("make_names")
    val makeNames: String?, // From GET list (e.g., "Aston Martin")
    @SerializedName("model_name")
    val modelName: String?,
    @SerializedName("year")
    val year: Int?,
    @SerializedName("keywords")
    val keywords: String?,
    @SerializedName("have_image")
    val haveImage: String?,
    @SerializedName("have_domain")
    val haveDomain: String?,

    // --- Fields for Add/Edit (from POST/PUT payload) ---
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("advertisement_goal_type_list_id") // This is the main Goal ID (e.g., 6)
    val advertisementGoalTypeListId: Int? = null,
    @SerializedName("advertisement_goal_type_id") // This is the sub-Goal ID (e.g., 2)
    val advertisementGoalTypeId: Int? = null,
    @SerializedName("url") // This is the 'goalUrl'
    val url: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("makes") // The POST/PUT expects a list of IDs
    val makes: List<Int>? = null,
    @SerializedName("model_id")
    val modelId: Int? = null,      // For POST/PUT
    @SerializedName("make_id") // This is NOT in the save payload but might be in GET details
    val makeId: Int? = null,

    // --- Common Admin Fields ---
    @SerializedName("is_deleted")
    val isDeleted: Boolean?,
    @SerializedName("created_by")
    val createdBy: Any?, // Can be Int or String
    @SerializedName("updated_by")
    val updatedBy: Any?, // Can be Int or String
    @SerializedName("created_on")
    val createdOn: Long?,
    @SerializedName("updated_on")
    val updatedOn: Long?
)

/**
 * Represents pagination info from the API.
 */
//data class Pagination(
//    @SerializedName("page")
//    val page: Int,
//    @SerializedName("total")
//    val total: Int
//)

/**
 * Represents an Advertisement Goal (from systems-api).
 */
data class AdvertisementGoal(
    @SerializedName("id")
    val id: Int, // This is an Int
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("icon")
    val icon: String?
)

/**
 * Represents an Advertisement Goal Type (from systems-api).
 */
data class AdvertisementGoalType(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String
)

/**
 * Wraps the list response for Ad Domains.
 */
data class AdvertisementDomainResponse(
    @SerializedName("list")
    val list: List<AdvertisementDomain>?
)

/**
 * Represents a single Ad Domain.
 */
data class AdvertisementDomain(
    @SerializedName("id")
    val id: String?,
    @SerializedName("url")
    val url: String?
)

/**
 * Represents the request body for updating domains.
 */
data class UpdateDomainsRequest(
    @SerializedName("list")
    val list: List<Int>
)

data class AdvDomain(
    @SerializedName("id")
    val id: Int,
    @SerializedName("domain_name")
    val domainName: String,
    @SerializedName("product_type_name")
    val productTypeName: String?
)

/**
 * Wraps the list response for Ad Gallery.
 */


/**
 * Represents a single Ad Image.
 */
data class AdvertisementImage(
    @SerializedName("id")
    val id: String?,
    @SerializedName("image_url")
    val imageUrl: String?
)


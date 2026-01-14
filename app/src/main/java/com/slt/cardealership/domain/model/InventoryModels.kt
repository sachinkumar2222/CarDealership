package com.slt.cardealership.domain.model

import com.google.gson.annotations.SerializedName

data class DomainInventorySetting(
    @SerializedName("domain_id") val domainId: Int,
    @SerializedName("default_inventory_view") val defaultInventoryView: String?, // "grid" or "list"
    @SerializedName("default_order") val defaultOrder: String?,
    @SerializedName("vdp_navigation") val vdpNavigation: String?,
    @SerializedName("is_client") val isClient: String? = "no"
)

data class DomainResearchSetting(
    @SerializedName("domain_id") val domainId: Int,
    @SerializedName("show_discontinued_makes") val showDiscontinuedMakes: Boolean?,
    @SerializedName("min_year") val minYear: Int?,
    @SerializedName("domain") val domain: String?
)

data class Make(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String
)

data class BodyType(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

data class DomainMakeSetting(
    @SerializedName("make_id") val makeId: Int,
    @SerializedName("vehicle_module") val vehicleModule: String, // "inventory"
    @SerializedName("condition") val condition: String, // "default", "new", "used"
    @SerializedName("make_name") val makeName: String? = null
)

// Wrapper for saving list of makes
data class SaveDomainMakesRequest(
    @SerializedName("domain_id") val domainId: String,
    @SerializedName("vehicle_module") val vehicleModule: String,
    @SerializedName("condition") val condition: String? = null,
    @SerializedName("makes") val makeIds: List<Int>
)
data class DomainBodyTypeSetting(
    @SerializedName("body_type_id") val bodyTypeId: Int,
    @SerializedName("vehicle_module") val vehicleModule: String, // "inventory"
    @SerializedName("body_type_name") val bodyTypeName: String? = null
)

data class SaveDomainBodyTypesRequest(
    @SerializedName("domain_id") val domainId: String,
    @SerializedName("vehicle_module") val vehicleModule: String,
    @SerializedName("body_types") val bodyTypeIds: List<Int>
)

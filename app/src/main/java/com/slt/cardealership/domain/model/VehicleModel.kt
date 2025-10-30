package com.slt.cardealership.domain.model

import com.google.gson.annotations.SerializedName

// Represents a single model object from the API response
data class VehicleModel(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("slug")
    val slug: String
)

data class VehicleModelListResponse(
    val list: List<VehicleModel> // Assuming a 'list' key, adjust if needed
)
package com.slt.cardealership.domain.model

import com.google.gson.annotations.SerializedName

data class VehicleListResponse(
    @SerializedName("list") // This matches your repository code 'response.list'
    val list: List<Vehicle>,

    // Your API might also include count or pagination info, add as needed
    @SerializedName("count")
    val count: Int? = null,

    @SerializedName("total_pages")
    val totalPages: Int? = null
)
package com.slt.cardealership.domain.model
import com.google.gson.annotations.SerializedName

// Example for: GET /vin-decoder-api/GetTrimListByVIN
data class TrimListResponse(
    @SerializedName("trims")
    val trims: List<TrimData>,
    @SerializedName("vin")
    val vin: String
)

data class TrimData(
    @SerializedName("trim_id")
    val trimId: String,
    @SerializedName("trim_name")
    val trimName: String
)
package com.slt.cardealership.domain.model
import com.google.gson.annotations.SerializedName

// Example for: POST /vin-decoder-api/GetMappedDecoderData
data class VinRequest(
    @SerializedName("vin")
    val vin: String
)
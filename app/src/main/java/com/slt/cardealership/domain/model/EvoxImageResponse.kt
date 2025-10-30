package com.slt.cardealership.domain.model
import com.google.gson.annotations.SerializedName

// Example for: GET /vin-decoder-api/GetEvoxImagesForAddEdit
data class EvoxImageResponse(
    @SerializedName("images")
    val images: List<EvoxImage>
)

data class EvoxImage(
    @SerializedName("url")
    val url: String,
    @SerializedName("shot_id")
    val shotId: Int
)
package com.slt.cardealership.domain.model
import com.google.gson.annotations.SerializedName

// Example for: GET /vin-decoder-api/GetVehicleOptionsPackagesForEdit
data class VehicleOptionsResponse(
    @SerializedName("options")
    val options: List<VehicleOption>,
    @SerializedName("packages")
    val packages: List<VehiclePackage>
)

data class VehicleOption(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String
)

data class VehiclePackage(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String
)
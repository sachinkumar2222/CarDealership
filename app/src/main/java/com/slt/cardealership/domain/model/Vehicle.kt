package com.slt.cardealership.domain.model

import com.google.gson.annotations.SerializedName

// Represents the response from the API when fetching a list of vehicles
data class VehicleListResponse(
    @SerializedName("list")
    val list: List<Vehicle> = emptyList(),
    // You can add pagination info here if the API provides it
)

// Represents a single vehicle in the inventory
data class Vehicle(
    val id: String? = null,
    val vin: String? = null,
    val year: Int? = null,
    val make: String? = null,
    val model: String? = null,
    val trim: String? = null,
    val mileage: Int? = null,
    val price: Double? = null,
    @SerializedName("stock_number")
    val stockNumber: String? = null,
    @SerializedName("drivetrain")
    val driveTrain: String? = null,
    @SerializedName("body_type")
    val bodyType: String? = null,
    @SerializedName("transmission_type")
    val transmission: String? = null,
    @SerializedName("fuel_type")
    val fuelType: String? = null,
    @SerializedName("exterior_color")
    val exteriorColor: String? = null,
    @SerializedName("interior_color")
    val interiorColor: String? = null,
    @SerializedName("is_certified")
    val isCertified: Boolean? = false,
    val status: String? = null,
    val condition: String? = null,
    @SerializedName("days_in_inventory")
    val daysInInventory: Int? = 0,
    @SerializedName("images_count")
    val imageCount: Int? = 0,
    @SerializedName("preview_image_url") // Assuming API provides a main image
    val previewImageUrl: String? = null,
    @SerializedName("created_at")
    val createdOn: String? = null,
    @SerializedName("updated_at")
    val updatedOn: String? = null,
    // Add other fields from your extensive list as needed
)

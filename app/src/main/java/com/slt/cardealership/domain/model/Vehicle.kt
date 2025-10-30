package com.slt.cardealership.domain.model

import com.google.gson.annotations.SerializedName

data class Vehicle(

    @SerializedName("id")
    val id: String?,
    @SerializedName("vin")
    val vin: String, // VIN is likely the only non-null key
    @SerializedName("make_id")
    val makeId: Int,
    @SerializedName("dealer_id")
    val dealerId: Int,
    @SerializedName("model_id")
    val modelId: Int,
    @SerializedName("year")
    val year: Int,
    @SerializedName("body_type_id")
    val bodyTypeId: Int,
    @SerializedName("fuel_type_id")
    val fuelTypeId: Int,
    @SerializedName("transmission_id")
    val transmissionId: Int,
    @SerializedName("drivetrain_id")
    val drivetrainId: Int,
    @SerializedName("certified")
    val certified: String?, // Changed to nullable
    @SerializedName("created_by")
    val createdBy: Int,
    @SerializedName("updated_by")
    val updatedBy: Int,
    @SerializedName("created_on")
    val createdOn: Long,
    @SerializedName("updated_on")
    val updatedOn: Long,
    @SerializedName("trim_name")
    val trimName: String?, // Changed to nullable
    @SerializedName("condition")
    val condition: String?, // Changed to nullable
    @SerializedName("status")
    val status: String?, // Changed to nullable
    @SerializedName("exterior_color_mfr")
    val exteriorColorMfr: String?, // Changed to nullable
    @SerializedName("interior_color_mfr")
    val interiorColorMfr: String?, // Changed to nullable
    @SerializedName("stock_no")
    val stockNo: String?, // Changed to nullable
    @SerializedName("engine_cylinders")
    val engineCylinders: Int,
    @SerializedName("doors")
    val doors: Int,
    @SerializedName("mileage")
    val mileage: Double?, // Changed to nullable
    @SerializedName("dealer_price")
    val dealerPrice: Double?, // Changed to nullable
    @SerializedName("retail_price")
    val retailPrice: Double?, // Changed to nullable
    @SerializedName("vdp_link")
    val vdpLink: String?, // Changed to nullable
    @SerializedName("thumbnail_image")
    val thumbnailImage: String?, // Changed to nullable

    @SerializedName("dealer_notes")
    val dealerNotesRaw: Any?, // Keep as Any?

    @SerializedName("feature")
    val featureRaw: Any?, // Keep as Any?

    @SerializedName("authorize_dealer")
    val authorizeDealer: Boolean,
    @SerializedName("carfax")
    val carfax: Boolean,
    @SerializedName("owner_cf")
    val ownerCf: Boolean,
    @SerializedName("auto_check")
    val autoCheck: Boolean,
    @SerializedName("dealer_certified")
    val dealerCertified: Boolean,
    @SerializedName("dealer_warranty")
    val dealerWarranty: Boolean,
    @SerializedName("factory_warranty")
    val factoryWarranty: Boolean,
    @SerializedName("green_vehicle")
    val greenVehicle: Boolean,
    @SerializedName("ext_warranty")
    val extWarranty: Boolean,
    @SerializedName("make_name")
    val brandName: String?, // Changed to nullable
    @SerializedName("brand_slug")
    val brandSlug: String?, // Changed to nullable
    @SerializedName("model_name")
    val modelName: String?, // Changed to nullable
    @SerializedName("model_slug")
    val modelSlug: String?, // Changed to nullable
    @SerializedName("trim_slug")
    val trimSlug: String?, // Changed to nullable
    @SerializedName("vehicleOptions")
    val vehicleOptions: List<String>?, // Keep nullable
    @SerializedName("vehiclePackages")
    val vehiclePackages: List<String>? // Keep nullable
) {
    val dealerNotesString: String
        get() = safeConvertToString(dealerNotesRaw)

    val featureString: String
        get() = safeConvertToString(featureRaw)

    private fun safeConvertToString(data: Any?): String {
        return when (data) {
            is String -> data
            is List<*> -> data.filterIsInstance<String>().joinToString(separator = ", ")
            else -> ""
        }
    }
}


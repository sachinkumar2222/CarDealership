package com.slt.cardealership.domain.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Wrapper response from the API.
 */
data class InternetLeadsResponse(
    val list: List<InternetLead>,
    val pagination: Pagination
)

data class Pagination(
    val total: Int
)

/**
 * A unified model representing any type of Internet Lead.
 * Fields that are specific to certain lead types are nullable.
 */
@Parcelize
data class InternetLead(
    @SerializedName("_id")
    val id: String,

    @SerializedName("lead_type")
    val leadType: String, // e.g., "sales-lead", "finance-lead"

    // --- Personal Info ---
    @SerializedName("first_name")
    val firstName: String?,
    @SerializedName("last_name")
    val lastName: String?,
    val email: String?,
    @SerializedName("phone_no")
    val phone: String?,
    val message: String?,

    // --- Meta Data ---
    @SerializedName("created_on")
    val createdOn: String?,
    val date: String?, // The formatted date string from server
    @SerializedName("is_read")
    val isRead: Boolean?,
    @SerializedName("is_sent")
    val isSent: Boolean?,

    // --- Vehicle Details (Sales, Vehicle, CashForCar, Service) ---
    val make: String?,
    val model: String?,
    val year: String?,
    val vin: String?,
    val mileage: Int?, // or String depending on API strictness

    // --- Finance Specific ---
    @SerializedName("credit_score")
    val creditScore: Int?,

    // --- Service Specific ---
    @SerializedName("service_date")
    val serviceDate: String?,

    // --- Build & Price / CashForCar Specific ---
    @SerializedName("file_url")
    val fileUrl: String?, // For uploaded files or images
    @SerializedName("image_url")
    val imageUrl: String?

) : Parcelable

/**
 * Helper enum to map UI display names to API values
 */
enum class LeadType(val apiValue: String, val displayName: String) {
    SALES("sales-lead", "Sales Leads"),
    FINANCE("finance-lead", "Finance Leads"),
    SERVICE("service-lead", "Service Leads"),
    CONTACT("contact-lead", "Contact Leads"),
    VEHICLE("vehicle-lead", "Vehicle Leads"),
    CASH_FOR_CAR("cashforcar-lead", "Cash For Car Leads"),
    BUILD_AND_PRICE("buildandprice-lead", "Build & Price Leads");

    companion object {
        fun fromApiValue(value: String): LeadType? = entries.find { it.apiValue == value }
    }
}

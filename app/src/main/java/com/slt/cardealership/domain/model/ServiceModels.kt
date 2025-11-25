package com.slt.cardealership.domain.model

import com.google.gson.annotations.SerializedName

/**
 * Represents a single product/service from the master list.
 * API: GET /systems-api/product-types/getAll
 */
data class ProductType(
    val id: Int,
    val name: String?,
    val slug: String?,
    @SerializedName("enable_as_service")
    val enableAsService: Boolean,
    @SerializedName("enable_lead_forms")
    val enableLeadForms: Boolean
)

/**
 * Represents the saved settings for a single service
 * that is *enabled* for the dealer.
 * API: GET /dealer-api/dealers/{id}/services
 */
data class DealerService(
    @SerializedName("product_type_id")
    val productTypeId: Int,
    val emails: List<String>?,
    @SerializedName("adf_emails")
    val adfEmails: List<String>?,
    val phones: List<String>?,
    @SerializedName("enable_lead_form")
    val enableLeadForm: Boolean,
    @SerializedName("primary_email")
    val primaryEmail: String?,
    @SerializedName("primary_adf_email")
    val primaryAdfEmail: String?,
    @SerializedName("primary_phone")
    val primaryPhone: String?
)

/**
 * --- NEW: Request Wrapper ---
 * Matches: {"dealer_id": 31181, "list": [...]}
 */
data class DealerServicesRequest(
    @SerializedName("dealer_id")
    val dealerId: Int,
    val list: List<DealerServicePayload>
)

/**
 * --- NEW: Request Item ---
 * Matches the specific format required for saving (String Yes/No).
 */
data class DealerServicePayload(
    @SerializedName("product_type_id")
    val productTypeId: Int,

    // Lists
    val emails: List<String>,
    @SerializedName("adf_emails")
    val adfEmails: List<String>,
    val phones: List<String>,

    // Important: This API expects "Yes" or "No", not Boolean
    @SerializedName("enable_lead_form")
    val enableLeadForm: String,

    @SerializedName("primary_email")
    val primaryEmail: String,
    @SerializedName("primary_adf_email")
    val primaryAdfEmail: String,
    @SerializedName("primary_phone")
    val primaryPhone: String
)
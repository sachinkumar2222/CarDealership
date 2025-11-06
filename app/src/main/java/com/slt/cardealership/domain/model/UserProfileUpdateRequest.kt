package com.slt.cardealership.domain.model

import com.google.gson.annotations.SerializedName

data class UserProfileUpdateRequest(
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("username") val username: String, // This is the email
    @SerializedName("role_id") val roleId: Int, // The numeric ID, not the name
    @SerializedName("created_by") val createdBy: Long,
    @SerializedName("created_on") val createdOn: Long, // Timestamp in seconds
    @SerializedName("updated_by") val updatedBy: Long,
    @SerializedName("updated_on") val updatedOn: Long, // Timestamp in seconds
    @SerializedName("organization_id") val organizationId: Long,
    @SerializedName("department_id") val departmentId: Int?, // Nullable
    @SerializedName("designation_id") val designationId: Int?, // Nullable
    @SerializedName("image_url") val imageUrl: String?, // Can be empty string
    @SerializedName("dealer_id") val dealerId: Long?, // Nullable
    @SerializedName("dealername") val dealerName: String?, // Can be empty string
    @SerializedName("is_active") val isActive: Boolean, // Boolean
    @SerializedName("gender") val gender: String?, // Can be empty string
    @SerializedName("language") val language: String?, // Can be empty string
    @SerializedName("phone") val phone: String?, // Can be empty string
    @SerializedName("address") val address: String?, // Can be empty string
    @SerializedName("doj") val doj: String?, // Can be empty string
    @SerializedName("dob") val dob: String?, // Can be empty string


)
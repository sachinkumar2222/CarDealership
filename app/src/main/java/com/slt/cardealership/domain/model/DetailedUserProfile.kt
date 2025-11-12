package com.slt.cardealership.domain.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

// This model is for the users/{id} response
@Parcelize
data class DetailedUserProfile(
    @SerializedName("id") val id: Long,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("username") val username: String,
    @SerializedName("phone") val phone: String?,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("role_type") val roleType: String,
    @SerializedName("role_id") val roleId: Int,
    @SerializedName("role_name") val roleName: String,
    @SerializedName("organization_id") val organizationId: Long,
    @SerializedName("organization_name") val organizationName: String?,
    @SerializedName("designation_id") val designationId: Int?,
    @SerializedName("designation_name") val designationName: String?,
    @SerializedName("department_id") val departmentId: Int?,
    @SerializedName("department_name") val departmentName: String?,
    @SerializedName("group_id") val groupId: Long?,
    @SerializedName("group_name") val groupName: String?,
    @SerializedName("created_by") val createdBy: Long?,
    @SerializedName("updated_by") val updatedBy: Long?,
    @SerializedName("created_by_email") val createdByEmail: String?,
    @SerializedName("updated_by_email") val updatedByEmail: String?,
    @SerializedName("created_on") val createdOn: Long?, // Timestamp
    @SerializedName("updated_on") val updatedOn: Long?, // Timestamp
    @SerializedName("last_logged_in") val lastLoggedIn: Long?, // Timestamp
    @SerializedName("image_url") val imageUrl: String?, // Profile picture URL
    @SerializedName("doj") val doj: String?, // Date of Joining
    @SerializedName("dob") val dob: String?, // Date of Birth
    @SerializedName("gender") val gender: String?,
    @SerializedName("language") val language: String?,
    @SerializedName("dealer_id") val dealerId: Long?,
    @SerializedName("dealer_name") val dealerName: String?,
    @SerializedName("address") val address: String?
) : Parcelable
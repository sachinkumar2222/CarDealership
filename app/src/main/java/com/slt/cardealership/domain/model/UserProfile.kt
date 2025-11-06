package com.slt.cardealership.domain.model

import com.google.gson.annotations.SerializedName

data class UserProfile(
    @SerializedName("userid") val userId: Long,
    @SerializedName("roleid") val roleId: Int,
    @SerializedName("username") val username: String,
    @SerializedName("usertype") val userType: String,
    @SerializedName("firstname") val firstName: String,
    @SerializedName("lastname") val lastName: String,
    @SerializedName("profilepic") val profilePic: String?, // Can be null or empty
    @SerializedName("rolename") val roleName: String,
    @SerializedName("allowedproject") val allowedProject: List<String>,
    @SerializedName("dealername") val dealerName: String?, // Can be null for non-dealers
    @SerializedName("dealeraddress") val dealerAddress: String?,
    @SerializedName("dealerslug") val dealerSlug: String?,
    @SerializedName("dealerid") val dealerId: Long?, // Nullable for non-dealers
    @SerializedName("dealergroupid") val dealerGroupId: Long?, // Nullable
    @SerializedName("organization_id") val organizationId: Long
)
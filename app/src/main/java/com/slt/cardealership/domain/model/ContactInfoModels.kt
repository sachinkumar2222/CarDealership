package com.slt.cardealership.domain.model

import com.google.gson.annotations.SerializedName

data class ContactInfoListResponse(
    @SerializedName("pagination") val pagination: Pagination,
    @SerializedName("list") val list: List<ContactDomainItem>
)

data class ContactDomainItem(
    @SerializedName("id") val id: Int,
    @SerializedName("label") val label: String,
    @SerializedName("phone_no") val phoneNo: String,
    @SerializedName("email") val email: String,
    @SerializedName("domain_name") val domainName: String? = null,
    @SerializedName("domain_id") val domainId: Int? = null
)

data class DomainContactRequest(
    @SerializedName("domain_id") val domainId: String,
    @SerializedName("label") val label: String,
    @SerializedName("phone_no") val phoneNo: String,
    @SerializedName("email") val email: String
)

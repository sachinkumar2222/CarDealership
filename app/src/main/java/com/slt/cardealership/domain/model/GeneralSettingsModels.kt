package com.slt.cardealership.domain.model

import com.google.gson.annotations.SerializedName

data class GeneralSettingsResponse(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("domain_id") val domainId: Int,
    @SerializedName("global_style") val globalStyle: String?,
    @SerializedName("header_script") val headerScript: String?,
    @SerializedName("footer_script") val footerScript: String?,
    @SerializedName("copyright_content") val copyrightContent: String?,
    @SerializedName("robots_meta_tags") val robotsMetaTags: List<String>?,
    @SerializedName("robots_file_content") val robotsFileContent: String?,
    @SerializedName("address") val address: String?,
    @SerializedName("advertisement_for") val advertisementFor: String? = null,
    @SerializedName("domain_used_for_transaction") val domainUsedForTransaction: String? = null,
    @SerializedName("sendgrid_key") val sendgridKey: String? = null,
    @SerializedName("created_by") val createdBy: Int? = null,
    @SerializedName("updated_by") val updatedBy: Int? = null
)

data class UpdateGeneralSettingsRequest(
    @SerializedName("domain_id") val domainId: Int,
    @SerializedName("global_style") val globalStyle: String?,
    @SerializedName("header_script") val headerScript: String?,
    @SerializedName("footer_script") val footerScript: String?,
    @SerializedName("copyright_content") val copyrightContent: String?,
    @SerializedName("robots_meta_tags") val robotsMetaTags: List<String>?,
    @SerializedName("robots_file_content") val robotsFileContent: String?,
    @SerializedName("address") val address: String?,
    @SerializedName("advertisement_for") val advertisementFor: String?,
    @SerializedName("domain_used_for_transaction") val domainUsedForTransaction: String?,
    @SerializedName("sendgrid_key") val sendgridKey: String?,
    @SerializedName("created_by") val createdBy: Int?,
    @SerializedName("updated_by") val updatedBy: Int?
)

package com.slt.cardealership.domain.model

import com.google.gson.annotations.SerializedName

data class DomainThemeSetting(
    @SerializedName("domain_id") val domainId: Int,
    @SerializedName("font_id") val fontId: Int?,
    @SerializedName("primary_color") val primaryColor: String?,
    @SerializedName("secondary_color") val secondaryColor: String?,
    @SerializedName("button_text_color") val buttonTextColor: String?,
    @SerializedName("line_height") val lineHeight: Double?,
    @SerializedName("letter_spacing") val letterSpacing: Double?,
    @SerializedName("font_weight") val fontWeight: Int?,
    @SerializedName("header_type") val headerType: String?,
    @SerializedName("footer_type") val footerType: String?,
    @SerializedName("show_footer_social_media") val showFooterSocialMedia: Boolean?,
    @SerializedName("show_top_bar") val showTopBar: Boolean?,
    @SerializedName("heading_font_id") val headingFontId: Int?,
    @SerializedName("heading_line_height") val headingLineHeight: Double?,
    @SerializedName("heading_font_weight") val headingFontWeight: Int?,
    @SerializedName("dark_logo_url") val darkLogoUrl: String?,
    @SerializedName("light_logo_url") val lightLogoUrl: String?,
    @SerializedName("og_logo_url") val ogLogoUrl: String?,
    @SerializedName("favicon_url") val faviconUrl: String?,
    @SerializedName("created_by") val createdBy: Int?,
    @SerializedName("updated_by") val updatedBy: Int?,
    @SerializedName("created_on") val createdOn: Long?,
    @SerializedName("updated_on") val updatedOn: Long?
)

data class DomainFont(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("domain_name") val domainName: String?,
    @SerializedName("file_url") val fileUrl: String?,
    @SerializedName("is_deleted") val isDeleted: Boolean?,
    @SerializedName("created_by") val createdBy: String?,
    @SerializedName("updated_by") val updatedBy: String?,
    @SerializedName("created_on") val createdOn: Long?,
    @SerializedName("updated_on") val updatedOn: Long?
)

data class DomainFontsResponse(
    @SerializedName("pagination") val pagination: Pagination,
    @SerializedName("list") val list: List<DomainFont>
)

data class DomainImageUploadResponse(
    @SerializedName("url") val url: String
)

data class DomainDefaultTheme(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

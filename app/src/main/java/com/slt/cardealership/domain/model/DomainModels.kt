package com.slt.cardealership.domain.model

import com.google.gson.annotations.SerializedName

data class DomainResponse(
    @SerializedName("pagination") val pagination: Pagination,
    @SerializedName("list") val list: List<DomainItem>
)

data class Pagination(
    @SerializedName("page") val page: Int,
    @SerializedName("total") val total: Int
)

data class DomainItem(
    @SerializedName("id") val id: Int,
    @SerializedName("application_name") val applicationName: String,
    @SerializedName("domain_name") val domainName: String,
    @SerializedName("reference_domain") val referenceDomain: String?,
    @SerializedName("product_type_name") val productTypeName: String,
    @SerializedName("category_name") val categoryName: String?,
    @SerializedName("category_slug") val categorySlug: String?,
    @SerializedName("is_sub_domain") val isSubDomain: Boolean,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("is_deleted") val isDeleted: Boolean,
    @SerializedName("in_business_listing") val inBusinessListing: Boolean,
    @SerializedName("created_by") val createdBy: String,
    @SerializedName("updated_by") val updatedBy: String,
    @SerializedName("created_on") val createdOn: Long,
    @SerializedName("updated_on") val updatedOn: Long
)

data class DomainPageResponse(
    @SerializedName("pagination") val pagination: Pagination,
    @SerializedName("list") val list: List<DomainPage>
)

data class DomainPage(
    @SerializedName("id") val id: String,
    @SerializedName("page_name") val pageName: String,
    @SerializedName("page_slug") val pageSlug: String,
    @SerializedName("status") val status: String,
    @SerializedName("page_type_id") val pageTypeId: String,
    @SerializedName("page_type_name") val pageTypeName: String,
    @SerializedName("created_on") val createdOn: Long,
    @SerializedName("updated_on") val updatedOn: Long,
    @SerializedName("created_by") val createdBy: String,
    @SerializedName("updated_by") val updatedBy: String,
    @SerializedName("insert_to_sitemap") val insertToSitemap: Boolean
)

data class DomainPageUpdateRequest(
    @SerializedName("id") val id: String,
    @SerializedName("page_name") val pageName: String,
    @SerializedName("page_description") val pageDescription: String?,
    @SerializedName("page_title") val pageTitle: String?,
    @SerializedName("meta_title") val metaTitle: String?,
    @SerializedName("meta_description") val metaDescription: String?,
    @SerializedName("robots_meta_tags") val robotsMetaTags: String?,
    @SerializedName("header_script") val headerScript: String?,
    @SerializedName("footer_script") val footerScript: String?,
    @SerializedName("page_type_id") val pageTypeId: String,
    @SerializedName("status") val status: String,
    @SerializedName("sql_domain_id") val sqlDomainId: Int,
    @SerializedName("insert_to_sitemap") val insertToSitemap: Boolean?,
    @SerializedName("featured_image") val featuredImage: String?,
    @SerializedName("slider_id") val sliderId: String?,
    @SerializedName("updated_by") val updatedBy: String,
    @SerializedName("updated_on") val updatedOn: Long,
    @SerializedName("banner_type") val bannerType: String?,
    @SerializedName("banner_url") val bannerUrl: String?,
    @SerializedName("created_by") val createdBy: String,
    @SerializedName("created_on") val createdOn: Long,
    @SerializedName("page_slug") val pageSlug: String,
    val featuredFilePath: String? = null,
    val bannerFilePath: String? = null
)

data class DomainPageDetails(
    @SerializedName("id") val id: String,
    @SerializedName("page_name") val pageName: String,
    @SerializedName("page_slug") val pageSlug: String,
    @SerializedName("status") val status: String,
    @SerializedName("page_type_id") val pageTypeId: String,
    @SerializedName("page_description") val pageDescription: String?,
    @SerializedName("page_title") val pageTitle: String?,
    @SerializedName("meta_title") val metaTitle: String?,
    @SerializedName("meta_description") val metaDescription: String?,
    @SerializedName("robots_meta_tags") val robotsMetaTags: String?,
    @SerializedName("header_script") val headerScript: String?,
    @SerializedName("footer_script") val footerScript: String?,
    @SerializedName("featured_image") val featuredImage: String?,
    @SerializedName("banner_type") val bannerType: String?,
    @SerializedName("banner_url") val bannerUrl: String?,
    @SerializedName("slider_id") val sliderId: String?,
    @SerializedName("insert_to_sitemap") val insertToSitemap: Boolean?,
    @SerializedName("created_by") val createdBy: String,
    @SerializedName("updated_by") val updatedBy: String,
    @SerializedName("created_on") val createdOn: Long,
    @SerializedName("updated_on") val updatedOn: Long
)

data class DomainSlider(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("sql_domain_id") val sqlDomainId: Int,
    @SerializedName("created_on") val createdOn: Long,
    @SerializedName("updated_on") val updatedOn: Long,
    @SerializedName("created_by") val createdBy: String?,
    @SerializedName("updated_by") val updatedBy: String?,
    @SerializedName("slide_count") val slideCount: Int
)

data class DomainPageCreateRequest(
    @SerializedName("page_name") val pageName: String,
    @SerializedName("page_description") val pageDescription: String?,
    @SerializedName("page_title") val pageTitle: String?,
    @SerializedName("meta_title") val metaTitle: String?,
    @SerializedName("meta_description") val metaDescription: String?,
    @SerializedName("robots_meta_tags") val robotsMetaTags: String?,
    @SerializedName("header_script") val headerScript: String?,
    @SerializedName("footer_script") val footerScript: String?,
    @SerializedName("page_type_id") val pageTypeId: String?,
    @SerializedName("status") val status: String,
    @SerializedName("sql_domain_id") val sqlDomainId: Int,
    @SerializedName("insert_to_sitemap") val insertToSitemap: Boolean?,
    @SerializedName("featured_image") val featuredImage: String?,
    @SerializedName("slider_id") val sliderId: String?,
    @SerializedName("updated_by") val updatedBy: String,
    @SerializedName("updated_on") val updatedOn: Long,
    @SerializedName("banner_type") val bannerType: String?,
    @SerializedName("banner_url") val bannerUrl: String?,
    @SerializedName("page_slug") val pageSlug: String,
    @SerializedName("created_by") val createdBy: String,
    @SerializedName("created_on") val createdOn: Long,
    val featuredFilePath: String? = null,
    val bannerFilePath: String? = null
)

// --- NEW SLIDER CLASSES ---

data class DomainSliderCreateRequest(
    @SerializedName("name") val name: String,
    @SerializedName("sql_domain_id") val sqlDomainId: String
)

data class DomainSlideItem(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("link") val link: String,
    @SerializedName("target") val target: String,
    @SerializedName("start_date") val startDate: Long,
    @SerializedName("end_date") val endDate: Long
)

data class DomainSliderDetails(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("sql_domain_id") val sqlDomainId: Int,
    @SerializedName("slides") val slides: List<DomainSlideItem>,
    @SerializedName("created_on") val createdOn: Long,
    @SerializedName("updated_on") val updatedOn: Long
)

data class DomainSliderCreateResponse(
    @SerializedName("id") val id: String
)

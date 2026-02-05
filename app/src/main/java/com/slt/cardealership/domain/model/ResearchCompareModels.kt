package com.slt.cardealership.domain.model

import com.google.gson.annotations.SerializedName

data class ResearchCompareItem(
    val id: String?,
    @SerializedName("category_name") val categoryName: String?,
    val trims: List<ResearchCompareTrim>?,
    @SerializedName("sql_domain_id") val sqlDomainId: Int?,
    @SerializedName("domain_name") val domainName: String?,
    @SerializedName("created_on") val createdOn: Long?,
    @SerializedName("updated_on") val updatedOn: Long?,
    @SerializedName("created_by") val createdBy: String?,
    @SerializedName("updated_by") val updatedBy: String?
)

data class ResearchCompareTrim(
    @SerializedName("make_name") val makeName: String?,
    @SerializedName("make_slug") val makeSlug: String?,
    @SerializedName("model_name") val modelName: String?,
    @SerializedName("model_slug") val modelSlug: String?,
    val year: Int?,
    @SerializedName("trim_name") val trimName: String?,
    @SerializedName("trim_slug") val trimSlug: String?
)

data class ResearchCompareCategoryResponse(
    @SerializedName("list") val list: List<ResearchCompareCategory>
)

data class ResearchCompareCategory(
    val id: String,
    val name: String?,
    val slug: String?
)

data class CreateResearchCompareRequest(
    val category: ResearchCompareCategoryRequest,
    val items: List<ResearchCompareTrim>,
    @SerializedName("created_by") val createdBy: String,
    @SerializedName("created_on") val createdOn: Long,
    @SerializedName("domain_name") val domainName: String,
    @SerializedName("sql_domain_id") val sqlDomainId: Int,
    @SerializedName("updated_by") val updatedBy: String,
    @SerializedName("updated_on") val updatedOn: Long
)

data class ResearchCompareCategoryRequest(
    val id: String,
    @SerializedName("category_name") val categoryName: String,
    @SerializedName("category_slug") val categorySlug: String
)

// --- New Models for Edit/Details ---

data class ResearchCompareDetailsResponse(
    @SerializedName("_id") val iid: IdObject?, // Parsing the _id object
    val category: ResearchCompareCategoryDetails?,
    val items: List<ResearchCompareTrim>?,
    @SerializedName("sql_domain_id") val sqlDomainId: Int?,
    @SerializedName("domain_name") val domainName: String?,
    @SerializedName("created_on") val createdOn: Long?,
    @SerializedName("updated_on") val updatedOn: Long?,
    @SerializedName("created_by") val createdBy: String?,
    @SerializedName("updated_by") val updatedBy: String?
)

data class IdObject(
    val timestamp: Long?,
    val creationTime: String?
)

data class ResearchCompareCategoryDetails(
    val id: String?,
    @SerializedName("category_name") val categoryName: String?,
    @SerializedName("category_slug") val categorySlug: String?
)



// --- Research Blog Category Models ---

data class ResearchBlogCategoryResponse(
    val list: List<ResearchBlogCategory>,
    val pagination: Pagination?
)

data class ResearchBlogCategory(
    val id: String?,
    val name: String?,
    val slug: String?,
    val description: String?,
    @SerializedName("sql_domain_id") val sqlDomainId: Int?,
    @SerializedName("domain_name") val domainName: String?,
    @SerializedName("created_on") val createdOn: Long?,
    @SerializedName("updated_on") val updatedOn: Long?,
    @SerializedName("created_by") val createdBy: String?,
    @SerializedName("updated_by") val updatedBy: String?
)

data class CreateResearchBlogCategoryRequest(
    @SerializedName("domain_id") val domainId: Int,
    @SerializedName("domain_name") val domainName: String,
    val name: String,
    val description: String
)

data class UpdateResearchBlogCategoryRequest(
    val id: String,
    @SerializedName("domain_id") val domainId: Int,
    @SerializedName("domain_name") val domainName: String,
    val name: String,
    val description: String
)


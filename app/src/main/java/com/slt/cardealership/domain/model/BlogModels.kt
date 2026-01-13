package com.slt.cardealership.domain.model

import com.google.gson.annotations.SerializedName

data class DomainBlogResponse(
    @SerializedName("pagination") val pagination: Pagination,
    @SerializedName("list") val list: List<DomainBlog>
)

data class DomainBlog(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("author_name") val authorName: String?,
    @SerializedName("categories") val categories: String?,
    @SerializedName("blog_type") val blogType: String,
    @SerializedName("status") val status: String,
    @SerializedName("created_by") val createdBy: String,
    @SerializedName("updated_by") val updatedBy: String,
    @SerializedName("created_on") val createdOn: Long,
    @SerializedName("updated_on") val updatedOn: Long
)

data class BlogCategoryResponse(
    @SerializedName("pagination") val pagination: Pagination,
    @SerializedName("list") val list: List<BlogCategory>
)

data class BlogCategory(
    @SerializedName("id") private val _id: Any,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("sql_domain_id") val sqlDomainId: Int,
    @SerializedName("description") val description: String?,
    @SerializedName("created_by") val createdBy: String,
    @SerializedName("updated_by") val updatedBy: String,
    @SerializedName("created_on") val createdOn: Long,
    @SerializedName("updated_on") val updatedOn: Long
) {
    val id: String
        get() {
            return if (_id is String) {
                _id
            } else if (_id is Map<*, *>) {
                _id["\$oid"] as? String ?: _id.toString()
            } else {
                _id.toString()
            }
        }
}

data class BlogCta(
    @SerializedName("label") val label: String,
    @SerializedName("url") val url: String
)

data class BlogResearchCompare(
    @SerializedName("make_name") val makeName: String,
    @SerializedName("make_slug") val makeSlug: String,
    @SerializedName("model_name") val modelName: String,
    @SerializedName("model_slug") val modelSlug: String,
    @SerializedName("trim_name") val trimName: String,
    @SerializedName("trim_slug") val trimSlug: String,
    @SerializedName("year") val year: Int
)

data class DomainBlogDetails(
    @SerializedName("_id") val id: Map<String, Any>?,
    @SerializedName("title") val title: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("featured_image") val featuredImage: String?,
    @SerializedName("author_name") val authorName: String?,
    @SerializedName("status") val status: String,
    @SerializedName("description") val description: String,
    @SerializedName("short_description") val shortDescription: String?,
    @SerializedName("categories") val categories: List<BlogCategory>?,
    @SerializedName("tags") val tags: List<String>?,
    @SerializedName("meta_title") val metaTitle: String?,
    @SerializedName("meta_description") val metaDescription: String?,
    @SerializedName("created_on") val createdOn: Long,
    @SerializedName("updated_on") val updatedOn: Long,
    @SerializedName("created_by") val createdBy: String,
    @SerializedName("updated_by") val updatedBy: String,
    @SerializedName("is_deleted") val isDeleted: Boolean,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("published_date") val publishedDate: Long?,
    @SerializedName("research_compare_data") val researchCompareData: List<BlogResearchCompare>?,
    @SerializedName("blog_type") val blogType: String,
    @SerializedName("blog_cta") val blogCta: List<BlogCta>?,
    @SerializedName("sql_domain_id") val sqlDomainId: Int,
    @SerializedName("domain_name") val domainName: String?
)

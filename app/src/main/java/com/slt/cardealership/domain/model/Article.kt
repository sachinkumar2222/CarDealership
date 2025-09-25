package com.slt.cardealership.domain.model

import com.google.gson.annotations.SerializedName

// This is the wrapper for the entire API response
data class ArticleListResponse(
    // THIS IS THE FIX: It tells the parser to find the key "list" in the JSON
    // and put its contents into our "articles" property.
    @SerializedName("list")
    val articles: List<Article>?
)

// This data class for a single article now needs to match the new JSON fields
data class Article(
    @SerializedName("id")
    val id: String,

    @SerializedName("postTitle")
    val title: String,

    @SerializedName("domain_name")
    val domainName: String,

    @SerializedName("status")
    val status: String,

    // Use the formatted "createdOn" from the new response
    @SerializedName("createdOn")
    val createdOn: String
)
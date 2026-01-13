package com.slt.cardealership.domain.model

import com.google.gson.annotations.SerializedName

data class ResearchMake(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String
)

data class ResearchModel(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String
)

data class ResearchYear(
    @SerializedName("id") val id: Int,
    @SerializedName("year") val year: Int
)

data class ResearchTrim(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String
)

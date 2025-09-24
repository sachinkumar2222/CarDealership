package com.slt.cardealership.domain.model

import com.google.gson.annotations.SerializedName

/**
 * Represents a single article post from the API.
 * The field names are mapped to the expected JSON keys.
 */
data class Article(
    @SerializedName("id") // Assuming there's a unique ID for each post
    val id: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("domainName") // Assuming a field for this based on the UI
    val domainName: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("createdOn") // For sorting
    val createdOn: String? // Assuming date is a string, can be changed to Date if needed
)

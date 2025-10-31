package com.slt.cardealership.domain.model

import com.google.gson.annotations.SerializedName

/**
 * This is the wrapper for the paginated list of FAQs.
 * GET /dealer-api/dealer-faqs
 */
data class FaqListResponse(
    val pagination: FaqPagination,
    val list: List<FaqItem>
)

data class FaqPagination(
    val page: Int,
    val total: Int
)

/**
 * This represents an FAQ item in the main list.
 * Note: created_by and updated_by are Strings in this response.
 */
data class FaqItem(
    val id: Int,
    val question: String,
    val answer: String,
    val domain_name: String?,
    val created_by: String?,
    val updated_by: String?,
    val created_on: Long,
    val updated_on: Long
)

/**
 * This represents the full FAQ object for details, adding, and updating.
 * Note: created_by and updated_by are Ints here.
 */
data class FaqDetails(
    val id: Int,
    val question: String,
    val answer: String,
    val domain_id: Int?,
    val dealer_id: Int,
    val created_by: Int?,
    val updated_by: Int?,
    val created_on: Long,
    val updated_on: Long
)

/**
 * This is the JSON payload for creating (POST) or updating (PUT) an FAQ.
 * Based on your logs.
 */
data class FaqRequest(
    val dealer_id: Int,
    val domain_id: Int,
    val question: String,
    val answer: String,
    val created_by: Int,
    val updated_by: Int,
    val created_on: Long,
    val updated_on: Long
)
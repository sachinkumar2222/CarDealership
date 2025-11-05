package com.slt.cardealership.domain.model

import com.google.gson.annotations.SerializedName

/**
 * Payload for POST /dealer-api/dealer-requests/modify-dealer
 */
data class ModifyDealerRequest(
    val dealer_id: String,
    val dealer_key: String,
    val old_value: String,
    val new_value: String,
    val dealer_name: String,
    val city_name: String,
    val state_name: String,
    val zip_code: String,
    val user: UserPayload
)

data class UserPayload(
    val first_name: String,
    val last_name: String,
    val email: String
)
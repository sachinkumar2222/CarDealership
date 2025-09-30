package com.slt.cardealership.utils

import com.auth0.android.jwt.JWT
import com.slt.cardealership.domain.model.User

object TokenParser {

    fun parse(token: String): User? {
        // This function is still correct
        try {
            val jwt = JWT(token)
            val id = jwt.getClaim("sub").asString()
            val name = jwt.getClaim("name").asString()
            val email = jwt.getClaim("email").asString()

            return User(
                id = id ?: "",
                accessToken = token,
                name = name ?: "Unknown User",
                email = email ?: "No email provided"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // --- THIS IS THE CORRECTED FUNCTION ---
    fun getDealerIdFromToken(token: String): Long? {
        return try {
            val jwt = JWT(token)
            // The claim is "extension_DealerId", and its value is a string like "31181".
            // We get it as a string and then safely convert it to a Long.
            jwt.getClaim("extension_DealerId").asString()?.toLongOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
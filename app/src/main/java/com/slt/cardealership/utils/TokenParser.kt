package com.slt.cardealership.utils

import com.auth0.android.jwt.JWT
import com.slt.cardealership.domain.model.User

/**
 * A utility object to decode a JWT token and extract user information
 * using the Auth0 JWTDecode library.
 */
object TokenParser {

    /**
     * Decodes the payload of a JWT token string.
     * @param token The JWT token string.
     * @return A User object containing the parsed claims, or null if parsing fails.
     */
    fun parse(token: String): User? {
        try {
            // The library does all the heavy lifting in this one line!
            val jwt = JWT(token)

            // Use the getClaim method to safely access the data.
            // The 'sub' claim is the standard field for the user's unique ID.
            val id = jwt.getClaim("sub").asString()
            val name = jwt.getClaim("name").asString()
            val email = jwt.getClaim("email").asString()

            // Construct the User object with all required fields.
            // Use the elvis operator (?: "") to provide a default empty string
            // if a claim is null, preventing nullability crashes.
            return User(
                id = id ?: "",
                accessToken = token, // The token itself is the access token
                name = name ?: "Unknown User",
                email = email ?: "No email provided"
            )

        } catch (e: Exception){
            // If the token is invalid, the library will throw this specific exception.
            e.printStackTrace()
            return null
        } catch (e: Exception) {
            // General catch block for any other unexpected errors
            e.printStackTrace()
            return null
        }
    }
}


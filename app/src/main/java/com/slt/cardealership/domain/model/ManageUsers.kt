package com.slt.cardealership.domain.model

/**
 * Represents a single user object from the API response.
 * Based on GET organizations-api/users
 */
data class ManageUsers(
    val id: Long,
    val first_name: String?,
    val last_name: String?,
    val username: String, // This is the email
    val phone: String?,
    val is_active: Boolean = false, // Default for partial response
    val role_type: String?,
    val role_id: Int = 0, // Default for partial response
    val role_name: String?,
    val organization_id: Int?,
    val organization_name: String?,
    val designation_id: Int?,
    val designation_name: String?,
    val department_id: Int?,
    val department_name: String?,
    val group_id: Int?, // Assuming Int, change if needed
    val group_name: String?,
    val created_by: Int?,
    val updated_by: Int?,
    val created_by_email: String?,
    val updated_by_email: String?,
    val created_on: Long?,
    val updated_on: Long?,
    val last_logged_in: Long?,
    val image_url: String?,
    val doj: Long?,
    val dob: Long?,
    val gender: String?,
    val language: String?,
    val dealer_id: Long?,
    val dealer_name: String?,
    val address: String?
)

/**
 * Represents the entire response for the paginated user list.
 * Based on GET organizations-api/users
 */
data class ManageUsersResponse(
    val pagination: Pagination,
    val list: List<ManageUsers>
)


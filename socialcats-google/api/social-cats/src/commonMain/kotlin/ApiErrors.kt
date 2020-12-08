package com.nicolasmilliard.socialcats.api

import kotlinx.serialization.Serializable

@Serializable
data class ApiErrors(
    val errors: List<ApiError>
)

@Serializable
data class ApiError(
    val category: String,
    val code: String
)

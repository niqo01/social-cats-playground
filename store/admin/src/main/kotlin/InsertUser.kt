package com.nicolasmilliard.socialcats.store

data class InsertUser(
    val uid: String,
    val name: String? = null,
    val phoneNumber: String? = null,
    val email: String? = null,
    val emailVerified: Boolean? = null,
    val photoUrl: String? = null
)

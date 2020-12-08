package com.nicolasmilliard.socialcats.auth

import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
data class UserMetadata(
    val createdAt: Date,
    val lastSignedInAt: Date
)

@JsonClass(generateAdapter = true)
data class UserRecord(
    val uid: String,
    val disabled: Boolean = false,
    val displayName: String?,
    val email: String?,
    val emailVerified: Boolean? = null,
    val phoneNumber: String?,
    val photoURL: String?,
    val metadata: UserMetadata,
    val providerData: List<UserInfo>?
)

@JsonClass(generateAdapter = true)
data class UserInfo(
    val displayName: String?,
    val email: String?,
    val phoneNumber: String?,
    val photoURL: String?,
    val providerId: String,
    val uid: String
)

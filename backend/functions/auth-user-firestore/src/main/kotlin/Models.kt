package com.nicolasmilliard.socialcats

import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
data class UserMetadata(
    val creationTime: Date,
    val lastSignInTime: Date
)

@JsonClass(generateAdapter = true)
data class UserRecord(
    val uid: String,
    val disabled: Boolean,
    val displayName: String?,
    val email: String?,
    val emailVerified: Boolean,
    val phoneNumber: String?,
    val photoURL: String?,
    val metadata: UserMetadata,
    val providerData: List<UserInfo>?
)

@JsonClass(generateAdapter = true)
data class UserInfo(
    val providerId: String,
    val uid: String
)

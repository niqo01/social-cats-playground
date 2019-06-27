package com.nicolasmilliard.socialcats

import java.util.Date

// Below are models serialized automatically from Gson.
// Due to Gson limited Kotlin support, be warn of:
// - making sure all properties are nullable
// - no default value are assigned to property
// - no delegate properties are used
// More information here: https://medium.com/@programmerr47/gson-unsafe-problem-d1ff29d4696f

data class RawUserMetadata(
    val creationTime: Date?,
    val lastSignInTime: Date?
)

data class RawUserRecord(
    val uid: String?,
    val disabled: Boolean?,
    val displayName: String?,
    val email: String?,
    val emailVerified: Boolean?,
    val phoneNumber: String?,
    val photoURL: String?,
    val metadata: RawUserMetadata?,
    val providerData: List<RawUserInfo>?
)

data class RawUserInfo(
    val providerId: String?,
    val uid: String?
)

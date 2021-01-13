package com.nicolasmilliard.socialcatsaws.imageupload

const val AUTHORIZATION_SCHEME = "Bearer"
fun bearer(authToken: String) = "$AUTHORIZATION_SCHEME $authToken"

package com.nicolasmilliard.socialcats.api

const val PRODUCTION_API = "https://searchapi-dot-sweat-monkey.appspot.com/"
const val AUTHORIZATION_HEADER = "Authorization"
const val AUTHORIZATION_SCHEME = "Bearer"

const val ERROR_CATEGORY_TECHNICAL = "TECHNICAL"
const val ERROR_CODE_INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR"

fun bearer(authToken: String) = "$AUTHORIZATION_SCHEME $authToken"

package com.nicolasmilliard.socialcatsaws.api

public const val AUTHORIZATION_SCHEME: String = "Bearer"
public fun bearer(authToken: String): String = "$AUTHORIZATION_SCHEME $authToken"

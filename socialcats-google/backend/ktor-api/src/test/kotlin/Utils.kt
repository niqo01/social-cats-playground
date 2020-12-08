package com.nicolasmilliard.socialcats.searchapi

import io.ktor.config.MapApplicationConfig
import kotlinx.serialization.json.Json

val json = Json.Default

fun MapApplicationConfig.setConfig() {
    put("env.isProduction", "false")
    put("google.initFirebaseApp", "false")
    put("google.projectId", "projectId")
    put("elasticSearch.useAws", "false")
    put("elasticSearch.endpoint", "http://endpoint")
    put("elasticSearch.apiKeyId", "apiKeyId")
    put("elasticSearch.apiKey", "apiKey")
}

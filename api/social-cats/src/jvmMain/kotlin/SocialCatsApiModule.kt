package com.nicolasmilliard.socialcats.search

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.create

object SocialCatsApiModule {
    @OptIn(UnstableDefault::class)
    fun searchService(client: Lazy<OkHttpClient>): SearchService {
        val contentType = "application/json; charset=utf-8".toMediaType()
        val retrofit = Retrofit.Builder()
            .baseUrl(PRODUCTION_API)
            .callFactory(object : Call.Factory {
                override fun newCall(request: Request): Call {
                    return client.value.newCall(request)
                }
            })
            .addConverterFactory(Json(JsonConfiguration.Stable.copy(
                ignoreUnknownKeys = true,
                isLenient = true,
                serializeSpecialFloatingPointValues = true,
                useArrayPolymorphism = true
            )).asConverterFactory(contentType))
            .build()

        return retrofit.create()
    }
}

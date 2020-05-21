package com.nicolasmilliard.socialcats.search

import com.nicolasmilliard.socialcats.api.buildRetrofit
import kotlinx.serialization.UnstableDefault
import okhttp3.OkHttpClient
import retrofit2.create

object SearchServiceModule {
    @OptIn(UnstableDefault::class)
    fun searchService(client: Lazy<OkHttpClient>): SearchService {
        val retrofit = buildRetrofit(client)
        return retrofit.create()
    }
}

package com.nicolasmilliard.socialcats.data

import org.apache.http.HttpRequestInterceptor

object ElasticServiceInterceptorModule {

    fun provideElasticServiceInterceptor(keyId: String, apiKey: String): HttpRequestInterceptor =
        ElasticServiceInterceptor(keyId, apiKey)
}

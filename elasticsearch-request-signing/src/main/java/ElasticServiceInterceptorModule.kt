package com.nicolasmilliard.socialcats.data

object ElasticServiceInterceptorModule {

    fun provideElasticServiceInterceptor(keyId: String, apiKey: String) = ElasticServiceInterceptor(keyId, apiKey)
}

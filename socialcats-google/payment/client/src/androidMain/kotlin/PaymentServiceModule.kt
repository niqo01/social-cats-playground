package com.nicolasmilliard.socialcats.payment

import com.nicolasmilliard.socialcats.api.buildRetrofit
import okhttp3.OkHttpClient
import retrofit2.create

object PaymentServiceModule {
    fun paymentService(client: Lazy<OkHttpClient>): PaymentService {
        val retrofit = buildRetrofit(client)
        return retrofit.create()
    }
}

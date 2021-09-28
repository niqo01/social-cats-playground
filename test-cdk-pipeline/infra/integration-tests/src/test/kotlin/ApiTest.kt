package com.nicolasmilliard.testcdkpipeline

import com.moczul.ok2curl.CurlInterceptor
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.create
import retrofit2.http.Headers
import java.time.Duration
import kotlin.test.*

internal class ApiTest {

    @Test
    fun testRoot(): Unit = runBlocking {
        val url = System.getenv()["API_URL"]!!
        // Try adding program arguments at Run/Debug configuration
        println("Program arguments: $url")

        val retrofit = Retrofit.Builder()
            .baseUrl("https://$url")
            .client(OkHttpClient.Builder()
                .callTimeout(Duration.ofSeconds(40))
                .addInterceptor(CurlInterceptor { message -> println("Ok2Curl: $message") })
                .build())
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()

        val api = retrofit.create<Api>()
        val root = api.getData()
        println("root: $root")
        assertFalse { root.isNullOrEmpty() }
    }

    interface Api {
        @Headers("Cache-Control: max-age=0")
        @GET("getData")
        suspend fun getData(): String
    }
}
package com.nicolasmilliard.socialcats.searchapi

import com.google.common.truth.Truth.assertThat
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Test
import java.nio.charset.StandardCharsets.UTF_8
import java.util.zip.GZIPInputStream

class FunctionalTest {

    val BYPASS_TOKEN = "123454321qwertyytrewq"

    @Test
    fun testRoot() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://searchapi-dot-sweat-monkey.appspot.com/")
            .build()

        client.newCall(request).execute().use { response ->
            assertThat(response.isSuccessful).isTrue()
            assertThat(response.body).isNotNull()
            assertThat(response.body!!.string()).isEqualTo("Pong")
        }
    }

    @Test
    fun testRootGzip() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://searchapi-dot-sweat-monkey.appspot.com/")
            .addHeader("Accept-Encoding", "gzip")
            .build()

        client.newCall(request).execute().use { response ->
            assertThat(response.isSuccessful).isTrue()
            assertThat(response.header("Content-Encoding")).isEqualTo("gzip")
            assertThat(response.header("Content-Length")).isEqualTo("24")
            assertThat(response.body).isNotNull()
            GZIPInputStream(response.body!!.byteStream()).bufferedReader(UTF_8).use {
                assertThat(it.readText()).isEqualTo("Pong")
            }
        }
    }

    @Test
    fun testSearchUnAuth() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://searchapi-dot-sweat-monkey.appspot.com/v1/search")
            .build()

        client.newCall(request).execute().use { response ->
            assertThat(response.isSuccessful).isFalse()
            assertThat(response.code).isEqualTo(401)
        }
    }

    @Test
    fun testSearchAuth() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://searchapi-dot-sweat-monkey.appspot.com/v1/search")
            .header("Authorization", "Bearer $BYPASS_TOKEN")
            .build()

        client.newCall(request).execute().use { response ->
            assertThat(response.isSuccessful).isTrue()
        }
    }

    @Test
    fun testSetuPaymentIntentNotFound() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://searchapi-dot-sweat-monkey.appspot.com/v1/payment/setupIntent")
            .header("Authorization", "Bearer $BYPASS_TOKEN")
            .post("".toRequestBody())
            .build()

        client.newCall(request).execute().use { response ->
            assertThat(response.isSuccessful).isFalse()
            assertThat(response.code).isEqualTo(404)
        }
    }
}

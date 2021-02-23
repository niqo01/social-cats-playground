package com.nicolasmilliard.socialcatsaws.pushnotification.models

import kotlinx.serialization.Serializable

@Serializable
public enum class Platform {
  ANDROID
}

@Serializable
public enum class IdProvider {
  FCM
}

@Serializable
public data class SendRegTokenRequest(
  val instanceId: String,
  val token: String,
  val provider: IdProvider,
  val platform: Platform,
  val appVersionCode: Int,
  val languageTag: String
)

@Serializable
public object SendRegTokenResult

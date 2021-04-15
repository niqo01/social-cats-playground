package com.nicolasmilliard.socialcatsaws.billing.models

import kotlinx.serialization.Serializable


@Serializable
public data class SendPurchaseRequest(
  val sku: String,
  val purchaseToken: String
)

@Serializable
public object SendPurchaseResult

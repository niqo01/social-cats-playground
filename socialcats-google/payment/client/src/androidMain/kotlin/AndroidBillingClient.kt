package com.nicolasmilliard.socialcats.payment

import android.content.Context
import androidx.lifecycle.LifecycleObserver
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import mu.KotlinLogging

private val log = KotlinLogging.logger {}

class AndroidBillingClient(private val context: Context) : LifecycleObserver {

    private lateinit var billingClient2: BillingClient

//    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
//    fun create() {
//        // Create a new BillingClient in onCreate().
//        // Since the BillingClient can only be used once, we need to create a new instance
//        // after ending the previous connection to the Google Play Store in onDestroy().
//        billingClient2 = BillingClient.newBuilder(context)
//            .setListener{ responseCode, purchases ->
//                purchaseChannel.offer(PurchaseResult(responseCode, purchases))
//            }
//            .build()
//        if (!billingClient2.isReady) {
//            billingClient2.startConnection(object : BillingClientStateListener {
//                override fun onBillingSetupFinished(billingResult: BillingResult) {
//                    val responseCode = billingResult.responseCode
//                    val debugMessage = billingResult.debugMessage
//                    log.debug {  "onBillingSetupFinished: $responseCode $debugMessage"}
//                    if (responseCode == BillingClient.BillingResponseCode.OK) {
//                        // The billing client is ready. You can query purchases here.
//                        querySkuDetails()
//                        queryPurchases()
//                    }
//                }
//                override fun onBillingServiceDisconnected() {
//                    log.debug {  "onBillingServiceDisconnected"}
//                    // TODO: Try connecting again with exponential backoff.
//                    // billingClient.startConnection(this)
//                }
//            })
//        }
//    }
//
//    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
//    fun destroy() {
//        if (billingClient.isReady) {
//            // BillingClient can only be used once.
//            // After calling endConnection(), we must create a new BillingClient.
//            billingClient.endConnection()
//        }
//    }
//
//    // Channel to receive PurchaseResult
//    private val purchaseChannel: Channel<PurchaseResult> = Channel(Channel.UNLIMITED)
//
//    private val billingClient = BillingClient.newBuilder(context)
//        .setListener { responseCode, purchases ->
//            purchaseChannel.offer(PurchaseResult(responseCode, purchases))
//        }.build()
//
//    suspend fun querySkuDetails(params: SkuDetailsParams): List<SkuDetails> {
//        return suspendCoroutine { continuation ->
//            billingClient.querySkuDetailsAsync(params) { responseCode, skuDetailsList ->
//                if (responseCode.isSuccess()) {
//                    continuation.resume(skuDetailsList!!)
//                } else {
//                    continuation.resumeWithException(Exception("responseCode: $responseCode"))
//                }
//            }
//        }
//    }
//
//    suspend fun queryPurchaseHistory(): List<Purchase> {
//        return suspendCoroutine { continuation ->
//            billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP) { responseCode, purchases ->
//                if (responseCode.isSuccess()) {
//                    continuation.resume(purchases)
//                } else {
//                    continuation.resumeWithException(Exception("responseCode: $responseCode"))
//                }
//            }
//        }
//    }
//
//    suspend fun consume(purchaseToken: String): String {
//        return suspendCoroutine { continuation ->
//            billingClient.consumeAsync(purchaseToken) { responseCode, purchaseToken ->
//                if (responseCode.isSuccess()) {
//                    continuation.resume(purchaseToken)
//                } else {
//                    continuation.resumeWithException(Exception("responseCode: $responseCode"))
//                }
//            }
//        }
//    }
//
//
//    fun startPurchaseFlow(activity: Activity, params: BillingFlowParams): PurchaseResult {
//        billingClient.launchBillingFlow(activity, params)
//        return purchaseChannel.receive()
//    }

    // Data class to wrap responseCode and purchases
    data class PurchaseResult(val result: BillingResult, val purchases: List<Purchase>?)
}

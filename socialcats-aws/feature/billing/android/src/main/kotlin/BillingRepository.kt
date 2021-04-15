package com.nicolasmilliard.socialcatsaws.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode.BILLING_UNAVAILABLE
import com.android.billingclient.api.BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED
import com.android.billingclient.api.BillingClient.BillingResponseCode.OK
import com.android.billingclient.api.BillingClient.BillingResponseCode.SERVICE_DISCONNECTED
import com.android.billingclient.api.BillingClient.BillingResponseCode.SERVICE_TIMEOUT
import com.android.billingclient.api.BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import com.android.billingclient.api.querySkuDetails
import com.nicolasmilliard.socialcatsaws.billing.BillingRepository.Skus.SUBS_SKUS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.pow
import kotlin.time.seconds

public class BillingRepository(
    private val context: Context,
    private val billingWebService: BillingWebservice,
    private val scope: CoroutineScope
) {

    public data class PurchasesUpdate(
        val result: BillingResult,
        val purchases: List<Purchase>?
    )

    private val _purchasesUpdated = MutableSharedFlow<PurchasesUpdate>()
    private val purchasesUpdated = _purchasesUpdated.asSharedFlow()
    // init {
    //     scope.launch {
    //         purchasesUpdated.collect {
    //             when (it.result.responseCode) {
    //                 OK -> {
    //                     it.purchases!!.apply { processPurchases(toSet()) }
    //                 }
    //                 BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
    //                     //item already owned? call queryPurchasesAsync to verify and process all such items
    //                     Timber.d("already owned items")
    //                     val result = queryPurchases()
    //                     when(result){
    //                         QueryPurchasesResult.BillingError -> TODO()
    //                         QueryPurchasesResult.BillingUnavailable -> TODO()
    //                         QueryPurchasesResult.BillingUnsupported -> TODO()
    //                         is QueryPurchasesResult.Success -> processPurchases(result.purchases.toSet())
    //                     }
    //                 }
    //                 BillingClient.BillingResponseCode.DEVELOPER_ERROR -> {
    //                     Timber.e("Your app's configuration is incorrect. Review in the Google Play Console. Possible causes of this error include: APK is not signed with release key; SKU productId mismatch.")
    //                 }
    //                 else -> {
    //                     Timber.i("BillingClient.BillingResponse error code: ${it.result.responseCode}")
    //                 }
    //             }
    //         }
    //     }
    // }

    private val billingClient: Flow<BillingClientState> =
        billingClient { billingResult, purchases ->
            scope.launch {
                _purchasesUpdated.emit(PurchasesUpdate(billingResult, purchases))
            }
        }

    private fun billingClient(purchasesUpdated: PurchasesUpdatedListener): Flow<BillingClientState> =
        callbackFlow<BillingClientState> {
            val client = BillingClient.newBuilder(context)
                .setListener(purchasesUpdated)
                .enablePendingPurchases()
                .build()
            val clientListener = object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    when (result.responseCode) {
                        OK ->
                            offer(BillingClientState.Available(client))
                        BILLING_UNAVAILABLE -> {
                            offer(BillingClientState.Unsupported)
                            close()
                        }
                        else -> close(BillingException(result.responseCode, result.debugMessage))
                    }
                }

                override fun onBillingServiceDisconnected() {
                    close(BillingException(SERVICE_DISCONNECTED, "onBillingServiceDisconnected()"))
                }
            }
            client.startConnection(clientListener)
            awaitClose {
                if (client.isReady) {
                    client.endConnection()
                }
            }
        }.buffer(Channel.UNLIMITED)
            .retryWhen { cause, attempt ->
                if (cause is BillingException && cause.canBeRetried && attempt < 5) {
                    Timber.w(cause, "Error while setting up billing client. Retrying..")
                    val waitTime = (2f.pow(attempt.toInt()) * 500).toLong()
                    delay(waitTime)
                    return@retryWhen true
                } else {
                    Timber.e(cause, "Error while setting up billing client.")
                    return@retryWhen false
                }
            }.catch {
                if (it is BillingException) {
                    if (it.canBeRetried) emit(BillingClientState.NotAvailable) else emit(
                        BillingClientState.Error
                    )
                } else {
                    throw it
                }
            }
            .stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(
                    stopTimeoutMillis = 2.seconds.toLongMilliseconds(),
                    replayExpirationMillis = 0
                ),
                initialValue = BillingClientState.Initializing
            )

    private class BillingException(errorCode: Int, debugMessage: String) :
        Exception("Billing Error with code: $errorCode, debugMessage: $debugMessage") {
        val canBeRetried: Boolean = when (errorCode) {
            SERVICE_DISCONNECTED,
            SERVICE_TIMEOUT,
            SERVICE_UNAVAILABLE -> true
            else -> false
        }
    }

    public sealed class BillingClientState {
        public object Initializing : BillingClientState()
        public data class Available(val client: BillingClient) : BillingClientState()
        public object Unsupported : BillingClientState()
        public object NotAvailable : BillingClientState()
        public object Error : BillingClientState()
    }

    public suspend fun launchBillingFlow(
        activity: Activity,
        selectedSkuSub: String, currentSku: String? = null,
        currentPurchaseToken: String? = null
    ): LaunchBillingFlowResult {
        val builder = BillingFlowParams.newBuilder()
            .setSkuDetails(SkuDetails(selectedSkuSub))


        if (!currentSku.isNullOrEmpty() && !currentPurchaseToken.isNullOrEmpty()) {
            builder.setOldSku(currentSku, currentPurchaseToken)
        }

        val value = billingClient.filter {
            it !is BillingClientState.Initializing
        }.first()

        return when (value) {
            is BillingClientState.Available -> {
                value.client.launchBillingFlow(activity, builder.build())
                LaunchBillingFlowResult.Success
            }
            BillingClientState.Error -> LaunchBillingFlowResult.BillingError
            BillingClientState.Initializing -> throw IllegalStateException("Impossible")
            BillingClientState.NotAvailable -> LaunchBillingFlowResult.BillingUnavailable
            BillingClientState.Unsupported -> LaunchBillingFlowResult.BillingUnsupported

        }
    }

    public sealed class LaunchBillingFlowResult {
        public object Success : LaunchBillingFlowResult()
        public object BillingUnsupported : LaunchBillingFlowResult()
        public object BillingUnavailable : LaunchBillingFlowResult()
        public object BillingError : LaunchBillingFlowResult()
    }

    public suspend fun querySubscriptionsDetails(): QuerySkuDetailsResult {
        return querySkuDetails(BillingClient.SkuType.SUBS, SUBS_SKUS)
    }

    private suspend fun querySkuDetails(@BillingClient.SkuType skuType: String, skuList: List<String>): QuerySkuDetailsResult {
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(skuType)
        val value = billingClient.filter {
            it !is BillingClientState.Initializing
        }.first()
        return when (value) {
            is BillingClientState.Available -> {
                val result = value.client.querySkuDetails(params.build())
                when (result.billingResult.responseCode) {
                    OK -> return QuerySkuDetailsResult.Success(result.skuDetailsList!!.map {
                        Subscription(
                            sku = it.sku,
                            price = it.description,
                            title = it.title,
                            description = it.description,
                            originalJson = it.originalJson,
                            )
                    })
                    else -> {
                        Timber.e(
                            BillingException(
                                result.billingResult.responseCode,
                                result.billingResult.debugMessage
                            )
                        )
                        QuerySkuDetailsResult.BillingError
                    }
                }
            }
            is BillingClientState.Error -> QuerySkuDetailsResult.BillingError
            BillingClientState.Initializing -> throw IllegalStateException("Impossible")
            BillingClientState.NotAvailable -> QuerySkuDetailsResult.BillingUnavailable
            BillingClientState.Unsupported -> QuerySkuDetailsResult.BillingUnsupported
        }
    }

    public sealed class QuerySkuDetailsResult {
        public data class Success(val skuDetails: List<Subscription>) : QuerySkuDetailsResult()
        public object BillingUnsupported : QuerySkuDetailsResult()
        public object BillingUnavailable : QuerySkuDetailsResult()
        public object BillingError : QuerySkuDetailsResult()
    }

    public suspend fun queryPurchases(): QueryPurchasesResult {
        val value = billingClient.filter {
            it !is BillingClientState.Initializing
        }.first()

        return when (value) {
            is BillingClientState.Available -> {
                var result = value.client.queryPurchases(BillingClient.SkuType.SUBS)
                when (result.billingResult.responseCode) {
                    OK -> QueryPurchasesResult.Success(result.purchasesList!!)
                    else -> {
                        Timber.e(
                            BillingException(
                                result.billingResult.responseCode,
                                result.billingResult.debugMessage
                            )
                        )
                        QueryPurchasesResult.BillingError
                    }
                }
            }
            BillingClientState.Error -> QueryPurchasesResult.BillingError
            BillingClientState.Initializing -> throw IllegalStateException("Impossible")
            BillingClientState.NotAvailable -> QueryPurchasesResult.BillingUnavailable
            BillingClientState.Unsupported -> QueryPurchasesResult.BillingUnsupported
        }
    }

    public sealed class QueryPurchasesResult {
        public data class Success(val purchases: List<Purchase>) : QueryPurchasesResult()
        public object BillingUnsupported : QueryPurchasesResult()
        public object BillingUnavailable : QueryPurchasesResult()
        public object BillingError : QueryPurchasesResult()
    }

    private fun processPurchases(purchasesResult: Set<Purchase>) =
        CoroutineScope(Job() + Dispatchers.IO).launch {
            Timber.d("processPurchases called")
            val validPurchases = HashSet<Purchase>(purchasesResult.size)
            Timber.d("processPurchases newBatch content $purchasesResult")
            purchasesResult.forEach { purchase ->
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {

                    validPurchases.add(purchase)
                } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                    Timber.d("Received a pending purchase of SKU: ${purchase.sku}")
                    // handle pending purchases, e.g. confirm with users about the pending
                    // purchases, prompt them to complete it, etc.
                }
            }

            // TODO Acknowledge purchase
        }

    public suspend fun isSubscriptionSupported(): SubscriptionSupportedResult {
        val value = billingClient.filter {
            it !is BillingClientState.Initializing
        }.first()

        return when (value) {
            is BillingClientState.Available -> {
                var result =
                    value.client.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS)
                when (result.responseCode) {
                    OK -> SubscriptionSupportedResult.Success(true)
                    FEATURE_NOT_SUPPORTED -> SubscriptionSupportedResult.Success(false)
                    else -> {
                        Timber.e(
                            BillingException(
                                result.responseCode,
                                result.debugMessage
                            )
                        )
                        SubscriptionSupportedResult.BillingError
                    }
                }
            }
            BillingClientState.Error -> SubscriptionSupportedResult.BillingError
            BillingClientState.Initializing -> throw IllegalStateException("Impossible")
            BillingClientState.NotAvailable -> SubscriptionSupportedResult.BillingUnavailable
            BillingClientState.Unsupported -> SubscriptionSupportedResult.BillingUnsupported
        }
    }

    public sealed class SubscriptionSupportedResult {
        public data class Success(val subscriptionSupported: Boolean) :
            SubscriptionSupportedResult()

        public object BillingUnsupported : SubscriptionSupportedResult()
        public object BillingUnavailable : SubscriptionSupportedResult()
        public object BillingError : SubscriptionSupportedResult()
    }

    /**
     * [INAPP_SKUS], [SUBS_SKUS], [CONSUMABLE_SKUS]:
     *
     * If you don't need customization ,then you can define these lists and hardcode them here.
     * That said, there are use cases where you may need customization:
     *
     * - If you don't want to update your APK (or Bundle) each time you change your SKUs, then you
     *   may want to load these lists from your secure server.
     *
     * - If your design is such that users can buy different items from different Activities or
     * Fragments, then you may want to define a list for each of those subsets. I only have two
     * subsets: INAPP_SKUS and SUBS_SKUS
     */

    private object Skus {

        val PREMIUM_MONTHLY = "premium_monthly"
        val PREMIUM_YEARLY = "premium_yearly"

        val SUBS_SKUS = listOf(PREMIUM_MONTHLY, PREMIUM_YEARLY)
        val GOLD_STATUS_SKUS = SUBS_SKUS // coincidence that there only gold_status is a sub
    }

    public data class Subscription(
        val sku: String,
        val price: String,
        val title: String,
        val description: String,
        val originalJson: String
    )
}
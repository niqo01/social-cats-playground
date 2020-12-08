package com.nicolasmilliard.socialcats.payment.presenter

import com.nicolasmilliard.presentation.Presenter
import com.nicolasmilliard.socialcats.ConnectivityChecker
import com.nicolasmilliard.socialcats.payment.PaymentLoader
import com.nicolasmilliard.socialcats.payment.Price
import com.nicolasmilliard.socialcats.payment.presenter.CheckoutSubscriptionPresenter.Event
import com.nicolasmilliard.socialcats.payment.presenter.CheckoutSubscriptionPresenter.Model
import com.nicolasmilliard.socialcats.session.SessionAuthState
import com.nicolasmilliard.socialcats.session.SessionManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class CheckoutSubscriptionPresenter(
    private val sessionManager: SessionManager,
    private val paymentLoader: PaymentLoader,
    private val connectivityChecker: ConnectivityChecker
) : Presenter<Model, Event> {

    private val _models = MutableStateFlow(Model())
    override val models: StateFlow<Model> get() = _models

    private val _events = Channel<Event>(RENDEZVOUS)
    override val events: (Event) -> Unit get() = { _events.offer(it) }

    var launcher: Launcher? = null

    override suspend fun start() {
        coroutineScope {
            launch {
                sessionManager.sessions
                    .collect {
                        logger.info { "Payment Session: $it" }
                        val authToken =
                            if (it.hasAuthToken) (it.authState as SessionAuthState.Authenticated).authToken!! else null
                        if (authToken != null && _models.value.authToken == null) {
                            loadSubscriptionDetails(authToken)
                        }
                        _models.value = _models.value.copy(authToken = authToken)
                    }
            }

            launch {
                connectivityChecker.connectedStatus
                    .collect {
                        val hadConnectivity = _models.value.hasConnectivity
                        _models.value = _models.value.copy(hasConnectivity = it)
                        if (it && !hadConnectivity &&
                            _models.value.authToken != null &&
                            _models.value.noConnection &&
                            _models.value.prices == null
                        ) {
                            loadSubscriptionDetails(_models.value.authToken!!)
                        }
                    }
            }

            launch {
                _events.consumeEach {
                    when (it) {
                        is Event.Retry -> {
                            loadSubscriptionDetails(_models.value.authToken!!)
                        }
                        is Event.PayClick -> {
                            val priceId = it.priceId
                            createCheckoutSession(priceId)
                        }
                        is Event.ClearRequireConfirmation -> {
                            _models.value = _models.value.copy(requireConfirmation = null)
                        }
                        is Event.ClearStripeError -> {
                            _models.value = _models.value.copy(stripeErrorCode = null)
                        }
                    }
                }
            }
        }
    }

    private suspend fun loadSubscriptionDetails(token: String) = coroutineScope {
        launch {
            _models.value = _models.value.copy(isLoading = true, noConnection = false)
            paymentLoader.loadSubscriptionDetail(token).collect {
                when (it) {
                    is PaymentLoader.Status.Success -> {
                        _models.value = _models.value.copy(isLoading = false, prices = it.data.prices)
                    }
                    is PaymentLoader.Status.Failure -> {
                        logger.error(it.exception) { "Error while loading subscriptions details" }
                        _models.value = _models.value.copy(isLoading = false, noConnection = true)
                    }
                }
            }
        }
    }

    private suspend fun createCheckoutSession(priceId: String) = coroutineScope {
        launch {
            _models.value = _models.value.copy(isLoading = true)
            val token = _models.value.authToken!!
            paymentLoader.createCheckoutSession(token, priceId).collect {
                when (it) {
                    is PaymentLoader.Status.Success -> {
                        val url = it.data.checkoutUrl
                        launcher!!.startCheckout(url)
                    }
                    is PaymentLoader.Status.Failure -> {
                        logger.error(it.exception) { "Error while creating checkout session" }
                        _models.value = _models.value.copy(isLoading = false, noConnection = true)
                    }
                }
            }
        }
    }

    sealed class Event {
        object Retry : Event()
        data class PayClick(val priceId: String) : Event()
        object ClearRequireConfirmation : Event()
        object ClearStripeError : Event()
    }

    data class Model(
        val isLoading: Boolean = true,
        val authToken: String? = null,
        val noConnection: Boolean = false,
        val hasConnectivity: Boolean = false,
        val prices: List<Price>? = null,
        val selectedPaymentMethodId: String? = null,
        val requireConfirmation: RequireConfirmation? = null,
        val stripeErrorCode: String? = null
    )

    data class RequireConfirmation(
        val selectedPaymentMethodId: String,
        val clientSecret: String
    )

    interface Launcher {
        fun startCheckout(url: String): Boolean
    }
}

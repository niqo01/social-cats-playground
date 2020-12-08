package com.nicolasmilliard.socialcats.payment.presenter

import com.nicolasmilliard.presentation.Presenter
import com.nicolasmilliard.socialcats.ConnectivityChecker
import com.nicolasmilliard.socialcats.payment.PaymentLoader
import com.nicolasmilliard.socialcats.payment.presenter.ManageSubscriptionPresenter.Event
import com.nicolasmilliard.socialcats.payment.presenter.ManageSubscriptionPresenter.Model
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

class ManageSubscriptionPresenter(
    private val sessionManager: SessionManager,
    private val paymentLoader: PaymentLoader,
    private val connectivityChecker: ConnectivityChecker
) : Presenter<Model, Event> {

    private val _models = MutableStateFlow(Model())
    override val models: StateFlow<Model> get() = _models

    private val _events = Channel<Event>(RENDEZVOUS)
    override val events: (Event) -> Unit get() = { _events.offer(it) }

    override suspend fun start(): Unit = coroutineScope {
        launch {
            sessionManager.sessions
                .collect {
                    logger.info { "Payment Session: $it" }

                    if (it.hasAuthToken) {
                        val authToken = (it.authState as SessionAuthState.Authenticated).authToken
                        if (it.isAuthWithUser) {
                            val user = (it.authState as SessionAuthState.Authenticated.User).user
                            _models.value =
                                _models.value.copy(isLoading = false, authToken = authToken, isSubscribed = user!!.isMember)
                        } else {
                            _models.value = _models.value.copy(isLoading = false, authToken = authToken)
                        }
                    } else {
                        _models.value = _models.value.copy(isLoading = false)
                    }
                }
        }

//        launch {
//            connectivityChecker.connectedStatus
//                .collect {
//                    val hadConnectivity = _models.value.hasConnectivity
//                    _models.value = _models.value.copy(hasConnectivity = it)
//                    if (it && !hadConnectivity &&
//                        _models.value.isAuthenticated &&
//                        _models.value.noConnection &&
//                        _models.value.prices == null
//                    ) {
//                        loadSubscriptionDetails(_models.value.authToken!!)
//                    }
//                }
//        }

        launch {
            _events.consumeEach {
                when (it) {
                    is Event.Retry -> {
//                        loadSubscriptionDetails(_models.value.authToken!!)
                    }
                    is Event.CancelClick -> {
                        val token = _models.value.authToken!!
                        cancelSubscription(token)
                    }
                }
            }
        }
    }

    private suspend fun cancelSubscription(token: String) = coroutineScope {
        launch {
            _models.value = _models.value.copy(cancelling = true)
            paymentLoader.cancelSubscription(token).collect {
                when (it) {
                    is PaymentLoader.Status.Success -> {
                        _models.value = _models.value.copy(cancelling = false)
                    }
                    is PaymentLoader.Status.Failure -> {
                        logger.error(it.exception) { "Error while loading subscriptions details" }
                        _models.value = _models.value.copy(cancelling = false, noConnection = true)
                    }
                }
            }
        }
    }

    sealed class Event {
        object Retry : Event()
        object CancelClick : Event()
    }

    data class Model(
        val isLoading: Boolean = true,
        val authToken: String? = null,
        val noConnection: Boolean = false,
        val hasConnectivity: Boolean = false,
        val isSubscribed: Boolean = false,
        val cancelling: Boolean = false
    )
}

package com.nicolasmilliard.socialcats.search.presenter

import com.nicolasmilliard.presentation.Presenter
import com.nicolasmilliard.socialcats.auth.Auth
import com.nicolasmilliard.socialcats.auth.AuthCredential
import com.nicolasmilliard.socialcats.auth.AuthState
import com.nicolasmilliard.socialcats.auth.ui.AuthUi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class MainPresenter(
    private val authUi: AuthUi,
    private val auth: Auth
) : Presenter<MainPresenter.Model, MainPresenter.Event> {

    private val _models = ConflatedBroadcastChannel<Model>()
    override val models: Flow<Model> get() = _models.asFlow().distinctUntilChanged()

    private val _events = Channel<Event>(RENDEZVOUS)
    override val events: (Event) -> Unit get() = { _events.offer(it) }

    override suspend fun start() {
        logger.info { "start" }
        coroutineScope {

            var model = Model()
            fun sendModel(newModel: Model) {
                model = newModel
                _models.offer(newModel)
            }

            sendModel(model)

            launch {
                var silentSignInJob: Job? = null
                auth.authStates
                    .collect {
                        if (it is AuthState.UnAuthenticated &&
                            (silentSignInJob == null ||
                                silentSignInJob?.isActive == false)
                        ) {
                            silentSignInJob = launch {
                                try {
                                    sendModel(model.copy(authStatus = Model.SignInStatus.SIGNING_IN))
                                    logger.info { "Start Silent Signin" }
                                    authUi.silentSignIn()
                                } catch (e: Throwable) {
                                    logger.warn(e) { "Silently sign in failed" }
//                                    try {
//                                        auth.signInAnonymously()
//                                    } catch (e: Throwable) {
//                                        logger.error(e) { "Anonymous sign in failed" }
//                                        sendModel(model.copy(authStatus = Model.SignInStatus.FAILED))
//                                    }
                                }
                                sendModel(model.copy(authStatus = Model.SignInStatus.IDLE))
                            }
                        }
                    }
            }

            launch {
                var signInJob: Job? = null
                _events.consumeEach {
                    when (it) {
                        is Event.AnonymousUpdateMergeConflict -> {
                            if (signInJob?.isActive != true) {
                                signInJob = launch {
                                    try {
                                        sendModel(model.copy(authConflictStatus = Model.AuthConflictStatus.RESOLVING_CONFLICT))
                                        auth.signInWithCredential(it.authCredential)
                                        sendModel(model.copy(authConflictStatus = Model.AuthConflictStatus.IDLE))
                                    } catch (e: Throwable) {
                                        logger.error(e) { "Sign in with Credential failed" }
                                        sendModel(model.copy(authConflictStatus = Model.AuthConflictStatus.FAILED))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    sealed class Event {
        object RetryAnonymousSignIn : Event()
        data class AnonymousUpdateMergeConflict(val authCredential: AuthCredential) : Event()
    }

    data class Model(
        val authStatus: SignInStatus = SignInStatus.IDLE,
        val authConflictStatus: AuthConflictStatus = AuthConflictStatus.IDLE
    ) {
        enum class SignInStatus {
            IDLE, SIGNING_IN, FAILED
        }

        enum class AuthConflictStatus {
            IDLE, RESOLVING_CONFLICT, FAILED
        }
    }
}

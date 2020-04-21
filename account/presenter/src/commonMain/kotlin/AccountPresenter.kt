package com.nicolasmilliard.socialcats.account.presenter

import com.nicolasmilliard.presentation.Presenter
import com.nicolasmilliard.socialcats.account.presenter.AccountPresenter.Event
import com.nicolasmilliard.socialcats.account.presenter.AccountPresenter.Model
import com.nicolasmilliard.socialcats.account.presenter.AccountPresenter.Model.LoadingStatus.IDLE
import com.nicolasmilliard.socialcats.account.presenter.AccountPresenter.Model.LoadingStatus.LOADING
import com.nicolasmilliard.socialcats.account.presenter.AccountPresenter.Model.ProcessingStatus
import com.nicolasmilliard.socialcats.auth.AuthRecentLoginRequiredException
import com.nicolasmilliard.socialcats.auth.ui.AuthUi
import com.nicolasmilliard.socialcats.session.Session
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

class AccountPresenter(
    private val authUi: AuthUi,
    private val sessionManager: SessionManager
) : Presenter<Model, Event> {

    private val _models = MutableStateFlow(Model())
    override val models: StateFlow<Model> get() = _models

    private val _events = Channel<Event>(RENDEZVOUS)
    override val events: (Event) -> Unit get() = { _events.offer(it) }

    override suspend fun start() {
        logger.info { "start" }
        coroutineScope {

            fun sendModel(newModel: Model) {
                _models.value = newModel
            }
            launch {
                sessionManager.sessions.collect {
                    sendModel(
                        _models.value.copy(
                            session = it,
                            loadingStatus = if (it.authState == SessionAuthState.Unknown) LOADING else IDLE
                        )
                    )
                }
            }

            launch {
                _events.consumeEach {
                    when (it) {
                        is Event.ClearErrorStatus -> {
                            sendModel(_models.value.copy(loadingStatus = IDLE, processingStatus = ProcessingStatus.IDLE))
                        }
                        is Event.ClearNeedRecentLogin -> {
                            sendModel(_models.value.copy(needRecentLogin = false))
                        }
                        is Event.SignOut -> {
                            launch {
                                onSignOut(::sendModel)
                            }
                        }
                        is Event.DeleteAccount -> {
                            launch {
                                onDeleteAccount(::sendModel)
                            }
                        }
                    }
                }
            }
        }
    }

    suspend fun onSignOut(sendModel: (Model) -> Unit) {
        sendModel(_models.value.copy(processingStatus = ProcessingStatus.PROCESSING))
        try {
            authUi.signOut()
            sendModel(_models.value.copy(processingStatus = ProcessingStatus.IDLE))
        } catch (e: Exception) {
            logger.error(e) { "Error while signing out" }
            sendModel(_models.value.copy(processingStatus = ProcessingStatus.FAILED_SIGN_OUT))
        }
    }

    suspend fun onDeleteAccount(sendModel: (Model) -> Unit) {
        sendModel(_models.value.copy(processingStatus = ProcessingStatus.PROCESSING))
        try {
            authUi.delete()
            sendModel(_models.value.copy(processingStatus = ProcessingStatus.IDLE))
        } catch (e: AuthRecentLoginRequiredException) {
            sendModel(_models.value.copy(processingStatus = ProcessingStatus.IDLE, needRecentLogin = true))
        } catch (e: Exception) {
            logger.error(e) { "Error while signing out" }
            sendModel(_models.value.copy(processingStatus = ProcessingStatus.FAILED_DELETE_ACCOUNT))
        }
    }

    sealed class Event {
        object ClearErrorStatus : Event()
        object ClearNeedRecentLogin : Event()
        object SignOut : Event()
        object DeleteAccount : Event()
    }

    data class Model(
        val session: Session? = null,
        val loadingStatus: LoadingStatus = LOADING,
        val processingStatus: ProcessingStatus = ProcessingStatus.IDLE,
        val showLinkAccount: Boolean = false,
        val needRecentLogin: Boolean = false
    ) {
        enum class LoadingStatus {
            IDLE, LOADING, FAILED
        }

        enum class ProcessingStatus {
            IDLE, PROCESSING, FAILED_SIGN_OUT, FAILED_DELETE_ACCOUNT
        }
    }
}

package com.nicolasmilliard.socialcats.account.presenter

import com.nicolasmilliard.presentation.Presenter
import com.nicolasmilliard.socialcats.account.presenter.AccountPresenter.Event
import com.nicolasmilliard.socialcats.account.presenter.AccountPresenter.Model
import com.nicolasmilliard.socialcats.account.presenter.AccountPresenter.Model.LoadingStatus.IDLE
import com.nicolasmilliard.socialcats.account.presenter.AccountPresenter.Model.LoadingStatus.LOADING
import com.nicolasmilliard.socialcats.account.presenter.AccountPresenter.Model.ProcessingStatus
import com.nicolasmilliard.socialcats.auth.ui.AuthUi
import com.nicolasmilliard.socialcats.session.Session
import com.nicolasmilliard.socialcats.session.SessionManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class AccountPresenter(
    private val authUi: AuthUi,
    private val sessionManager: SessionManager
) : Presenter<Model, Event> {

    private val _models = ConflatedBroadcastChannel(Model())
    override val models: Flow<Model> get() = _models.asFlow()

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

            launch {
                sessionManager.sessions.collect {
                    sendModel(model.copy(session = it, loadingStatus = IDLE))
                }
            }

            launch {
                _events.consumeEach {
                    when (it) {
                        is Event.ClearErrorStatus -> {
                            sendModel(model.copy(loadingStatus = IDLE, processingStatus = ProcessingStatus.IDLE))
                        }
                        is Event.SignOut -> {
                            launch {
                                onSignOut(::sendModel, model)
                            }
                        }
                        is Event.DeleteAccount -> {
                            launch {
                                onDeleteAccount(::sendModel, model)
                            }
                        }
                    }
                }
            }
        }
    }

    suspend fun onSignOut(sendModel: (Model) -> Unit, model: Model) {
        sendModel(model.copy(processingStatus = ProcessingStatus.PROCESSING))
        try {
            authUi.signOut()
            sendModel(model.copy(processingStatus = ProcessingStatus.IDLE))
        } catch (e: Exception) {
            logger.error(e) { "Error while signing out" }
            sendModel(model.copy(processingStatus = ProcessingStatus.FAILED_SIGN_OUT))
        }
    }

    suspend fun onDeleteAccount(sendModel: (Model) -> Unit, model: Model) {
        sendModel(model.copy(processingStatus = ProcessingStatus.PROCESSING))
        try {
            authUi.delete()
            sendModel(model.copy(processingStatus = ProcessingStatus.IDLE))
        } catch (e: Exception) {
            logger.error(e) { "Error while signing out" }
            sendModel(model.copy(processingStatus = ProcessingStatus.FAILED_DELETE_ACCOUNT))
        }
    }

    sealed class Event {
        object ClearErrorStatus : Event()
        object SignOut : Event()
        object DeleteAccount : Event()
    }

    data class Model(
        val session: Session? = null,
        val loadingStatus: LoadingStatus = LOADING,
        val processingStatus: ProcessingStatus = ProcessingStatus.IDLE,
        val showLinkAccount: Boolean = false
    ) {
        enum class LoadingStatus {
            IDLE, LOADING, FAILED
        }

        enum class ProcessingStatus {
            IDLE, PROCESSING, FAILED_SIGN_OUT, FAILED_DELETE_ACCOUNT
        }
    }
}

package com.nicolasmilliard.socialcats.search.presenter

import com.nicolasmilliard.presentation.Presenter
import com.nicolasmilliard.socialcats.ConnectivityChecker
import com.nicolasmilliard.socialcats.model.User
import com.nicolasmilliard.socialcats.search.SearchLoader
import com.nicolasmilliard.socialcats.search.presenter.SearchPresenter.Event
import com.nicolasmilliard.socialcats.search.presenter.SearchPresenter.Model
import com.nicolasmilliard.socialcats.search.presenter.SearchPresenter.Model.LoadingState.Failed
import com.nicolasmilliard.socialcats.search.presenter.SearchPresenter.Model.LoadingState.Loading
import com.nicolasmilliard.socialcats.search.presenter.SearchPresenter.Model.LoadingState.Success
import com.nicolasmilliard.socialcats.search.presenter.SearchPresenter.Model.RefreshState.FAILED
import com.nicolasmilliard.socialcats.search.presenter.SearchPresenter.Model.RefreshState.IDLE
import com.nicolasmilliard.socialcats.search.presenter.SearchPresenter.Model.RefreshState.LOADING
import com.nicolasmilliard.socialcats.session.SessionManager
import com.nicolasmilliard.socialcats.session.SessionState.Session
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class SearchPresenter(
    private val sessionManager: SessionManager,
    private val searchLoader: SearchLoader,
    private val connectivityChecker: ConnectivityChecker
) : Presenter<Model, Event> {

    private val _models = ConflatedBroadcastChannel(Model())
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

            launch {
                connectivityChecker.connectedStatus
                    .onEach { logger.info { "Network connected: $it" } }
                    .collect {
                        sendModel(model.copy(isConnected = it))
                    }
            }

            launch {
                var activeQuery = ""
                var activeQueryJob: Job? = null

                activeQueryJob?.cancel()
                activeQueryJob = launch {
                    onQueryChanged(activeQuery, ::sendModel, model)
                }

                _events.consumeEach {
                    logger.info { "start.launch.consumeEach" }
                    when (it) {
                        is Event.ClearRefreshStatus -> {
                            val hasData = model.loadingState is Success
                            if (hasData) {
                                sendModel(
                                    model.copy(
                                        loadingState = (model.loadingState as Success).copy(
                                            refreshing = IDLE
                                        )
                                    )
                                )
                            } else {
                                logger.warn { "Clearing refresh state while not in success state" }
                            }
                        }
                        is Event.QueryChanged -> {
                            val query = it.query
                            if (query != activeQuery) {
                                activeQuery = query
                                activeQueryJob?.cancel()
                                activeQueryJob = launch {
                                    onQueryChanged(query, ::sendModel, model)
                                }
                            }
                        }
                        is Event.Retry -> {
                            activeQueryJob?.cancel()
                            activeQueryJob = launch {
                                onQueryChanged(activeQuery, ::sendModel, model)
                            }
                        }
                    }
                }
            }
        }
    }

    suspend fun onQueryChanged(query: String, sendModel: (Model) -> Unit, model: Model) {
        val sessionState = sessionManager.sessions.first()
        val session = if (sessionState is Session) sessionState else null
        searchLoader.searchUsers(
            session?.authToken,
            query
        ).collect { result ->
            val hasData = model.loadingState is Success
            sendModel(
                when (result) {
                    SearchLoader.Status.InProgress -> {
                        if (hasData) {
                            model.copy(
                                loadingState = (model.loadingState as Success).copy(
                                    refreshing = LOADING
                                )
                            )
                        } else {
                            model.copy(
                                loadingState = Loading
                            )
                        }
                    }
                    is SearchLoader.Status.Success -> {
                        if (hasData) {
                            model.copy(
                                loadingState = (model.loadingState as Success).copy(
                                    refreshing = IDLE
                                )
                            )
                        } else {
                            model.copy(
                                loadingState = Success(
                                    result.data.totalHits, Model.QueryResults(
                                        query,
                                        result.data.users
                                    )
                                )
                            )
                        }
                    }
                    is SearchLoader.Status.Failure -> {
                        logger.error(result.exception) { "Search Loader failure" }
                        if (hasData) {
                            model.copy(
                                loadingState = (model.loadingState as Success).copy(
                                    refreshing = FAILED
                                )
                            )
                        } else {
                            model.copy(
                                loadingState = Failed
                            )
                        }
                    }
                })
        }
    }

    sealed class Event {
        data class QueryChanged(val query: String) : Event()
        object ClearRefreshStatus : Event()
        object Retry : Event()
    }

    data class Model(
        val isConnected: Boolean = true,
        val loadingState: LoadingState = Loading
    ) {
        data class QueryResults(
            val query: String = "",
            val items: List<User> = emptyList()
        )

        sealed class LoadingState {
            object Loading : LoadingState()
            data class Success(
                val count: Long = 0,
                val queryResults: QueryResults = QueryResults(),
                val refreshing: RefreshState = IDLE
            ) : LoadingState()

            object Failed : LoadingState()
        }

        enum class RefreshState {
            IDLE, LOADING, FAILED
        }
    }
}

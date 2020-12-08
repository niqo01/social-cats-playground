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
import com.nicolasmilliard.socialcats.session.Session
import com.nicolasmilliard.socialcats.session.SessionAuthState
import com.nicolasmilliard.socialcats.session.SessionManager
import com.nicolasmilliard.socialcats.util.IoException
import com.nicolasmilliard.socialcats.util.isCausedBy
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class SearchPresenter(
    private val sessionManager: SessionManager,
    private val searchLoader: SearchLoader,
    private val connectivityChecker: ConnectivityChecker
) : Presenter<Model, Event> {

    private val _models = MutableStateFlow<Model>(Model())
    override val models: StateFlow<Model> get() = _models

    private val _events = Channel<Event>(RENDEZVOUS)
    override val events: (Event) -> Unit get() = { _events.offer(it) }

    override suspend fun start() {
        coroutineScope {

            launch {
                connectivityChecker.connectedStatus
                    .collect {
                        _models.value = _models.value.copy(isConnected = it)
                    }
            }

            var activeQuery = ""
            var activeQueryJob: Job? = null
            launch {
                var session: Session? = null
                sessionManager.sessions
                    .collect {
                        if (!it.hasAuthToken) {
                            _models.value = _models.value.copy(loadingState = Model.LoadingState.UnAuthenticated)
                        } else if (session == null || !session!!.hasAuthToken) {
                            activeQueryJob?.cancel()
                            activeQueryJob = launch {
                                onQueryChanged(activeQuery)
                            }
                        }

                        session = it
                    }
            }

            launch {
                _events.consumeEach {
                    when (it) {
                        is Event.ClearRefreshStatus -> {
                            val hasData = _models.value.loadingState is Success
                            if (hasData) {
                                _models.value =
                                    _models.value.copy(
                                        loadingState = (_models.value.loadingState as Success).copy(
                                            refreshing = IDLE
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
                                    onQueryChanged(query)
                                }
                            }
                        }
                        is Event.Retry -> {
                            activeQueryJob?.cancel()
                            activeQueryJob = launch {
                                onQueryChanged(activeQuery)
                            }
                        }
                    }
                }
            }
        }
    }

    suspend fun onQueryChanged(query: String) {
        logger.info { "onQueryChanged query: $query" }
        val session = sessionManager.sessions
            .first()

        val token =
            if (session.authState is SessionAuthState.Authenticated) {
                (session.authState as SessionAuthState.Authenticated)
                    .authToken
            } else null

        if (token == null) {
            _models.value = _models.value.copy(loadingState = Model.LoadingState.UnAuthenticated)
            return
        }
        if (_models.value.loadingState is Success) {
            _models.value =
                _models.value.copy(
                    loadingState = (_models.value.loadingState as Success).copy(
                        refreshing = LOADING
                    )
                )
        } else {
            _models.value = _models.value.copy(loadingState = Loading)
        }

        searchLoader.searchUsers(token, query).collect { result ->
            val hasData = _models.value.loadingState is Success
            _models.value =
                when (result) {
                    SearchLoader.Status.InProgress -> {
                        if (hasData) {
                            _models.value.copy(
                                loadingState = (_models.value.loadingState as Success).copy(
                                    refreshing = LOADING
                                )
                            )
                        } else {
                            _models.value.copy(
                                loadingState = Loading
                            )
                        }
                    }
                    is SearchLoader.Status.Success -> {
                        if (hasData) {
                            _models.value.copy(
                                loadingState = (_models.value.loadingState as Success).copy(
                                    refreshing = IDLE
                                )
                            )
                        } else {
                            _models.value.copy(
                                loadingState = Success(
                                    result.data.totalHits,
                                    Model.QueryResults(
                                        query,
                                        result.data.users
                                    )
                                )
                            )
                        }
                    }
                    is SearchLoader.Status.Failure -> {
                        if (result.exception.isCausedBy(IoException::class)) {
                            logger.info(result.exception) { "Search Loader failure" }
                        } else {
                            logger.error(result.exception) { "Search Loader failure" }
                        }

                        if (hasData) {
                            _models.value.copy(
                                loadingState = (_models.value.loadingState as Success).copy(
                                    refreshing = FAILED
                                )
                            )
                        } else {
                            _models.value.copy(
                                loadingState = Failed
                            )
                        }
                    }
                }
        }
    }

    sealed class Event {
        data class QueryChanged(val query: String) : Event()
        object ClearRefreshStatus : Event()
        object Retry : Event()
    }

    data class Model(
        val isConnected: Boolean = false,
        val loadingState: LoadingState = Loading
    ) {
        data class QueryResults(
            val query: String = "",
            val items: List<User> = emptyList()
        )

        sealed class LoadingState {
            object UnAuthenticated : LoadingState()
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

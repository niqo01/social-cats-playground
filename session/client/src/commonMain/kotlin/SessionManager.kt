package com.nicolasmilliard.socialcats.session

import com.nicolasmilliard.socialcats.auth.Auth
import com.nicolasmilliard.socialcats.auth.AuthState.Authenticated
import com.nicolasmilliard.socialcats.auth.AuthState.UnAuthenticated
import com.nicolasmilliard.socialcats.store.DeviceInfo
import com.nicolasmilliard.socialcats.store.SocialCatsStore
import com.nicolasmilliard.socialcats.store.User
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class SessionManager(
    private val auth: Auth,
    private val store: SocialCatsStore,
    private val instanceIdProvider: InstanceIdProvider
) {

    private val _sessions = ConflatedBroadcastChannel<Session>()
    val sessions: Flow<Session> get() = _sessions.asFlow()

    private val _events = Channel<Event>(Channel.UNLIMITED)
    val events: (Event) -> Unit get() = { _events.offer(it) }

    suspend fun start() {
        logger.info { "start" }
        coroutineScope {
            var session = Session()

            fun sendSession(newSession: Session) {
                session = newSession
                _sessions.offer(session)
            }

            var tempToken: String? = null

            launch {
                var userJob: Job? = null
                auth.authStates
                    .collect { authState ->
                        when (authState) {
                            is UnAuthenticated -> {
                                userJob?.cancel()
                                sendSession(
                                    session.copy(
                                        authData = null
                                    )
                                )
                            }
                            is Authenticated -> {
                                sendSession(
                                    session.copy(
                                        authData = AuthData(authState.authToken?.token, authState.authUser.isAnonymous, null)
                                    )
                                )
                                userJob?.cancel()
                                userJob = launch {
                                    store.getCurrentUser(authState.authUser.uid)
                                        .collect {
                                            sendSession(session.copy(authData = session.authData!!.copy(user = it)))
                                        }
                                    if (tempToken != null) {
                                        events(Event.DeviceTokenChanged(tempToken!!))
                                    }
                                }
                            }
                        }
                    }
            }

            launch {
                val deviceInfo = UserDevice(instanceIdProvider.getId(), null, instanceIdProvider.languageTag)
                sendSession(session.copy(device = deviceInfo))
                if (tempToken != null) {
                    events(Event.DeviceTokenChanged(tempToken!!))
                }
            }

            launch {
                var storeTokenJob: Job? = null
                _events.consumeEach {
                    logger.info { "Trace 1" }
                    when (it) {
                        is Event.DeviceTokenChanged -> {
                            if (session.device?.token != it.newToken) {
                                if (session.authData?.user == null ||
                                    session.device?.deviceId == null
                                ) {
                                    tempToken = it.newToken
                                } else {
                                    tempToken = null
                                    storeTokenJob?.cancel()
                                    storeTokenJob = launch {
                                        store.saveInstanceId(
                                            session.authData!!.user!!.id,
                                            DeviceInfo(session.device!!.deviceId, it.newToken, session.device!!.languageTag)
                                        )
                                        sendSession(session.copy(device = session.device?.copy(token = it.newToken)))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun onNewToken(token: String) {
        logger.info { "3" }
        events(Event.DeviceTokenChanged(token))
    }
}

data class Session(
    val authData: AuthData? = null,
    val device: UserDevice? = null
) {
    val isAuthenticated = authData != null
}

data class AuthData(
    val authToken: String?,
    val isAnonymous: Boolean,
    val user: User? = null
)

sealed class Event {
    data class DeviceTokenChanged(val newToken: String) : Event()
}

interface InstanceIdProvider {
    suspend fun getId(): String
    val languageTag: String
}

data class UserDevice(
    val deviceId: String,
    val token: String?,
    val languageTag: String
)

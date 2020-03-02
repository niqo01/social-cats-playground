package com.nicolasmilliard.socialcats.session

import com.nicolasmilliard.socialcats.analytics.Analytics
import com.nicolasmilliard.socialcats.auth.Auth
import com.nicolasmilliard.socialcats.auth.AuthState
import com.nicolasmilliard.socialcats.store.DeviceInfo
import com.nicolasmilliard.socialcats.store.UserStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class SessionManager(
    private val auth: Auth,
    private val store: UserStore,
    private val deviceInfoProvider: DeviceInfoProvider,
    private val analytics: Analytics
) {

    private val _sessions = ConflatedBroadcastChannel<Session>()
    val sessions: Flow<Session> get() = _sessions.asFlow()

    private val _newTokens = Channel<String>(Channel.CONFLATED)
    val onNewTokens: (String) -> Unit get() = { _newTokens.offer(it) }

    suspend fun start() {
        logger.info { "start" }
        coroutineScope {
            var session = Session()

            fun sendSession(newSession: Session) {
                session = newSession
                logger.info {
                    "Sending session, Authenticated: ${session.isAuthenticated}, " +
                        " anonymous: ${session.authState is SessionAuthState.Authenticated.Anonymous}" +
                        ", Device: ${session.device}"
                }
                _sessions.offer(session)
            }

            launch {
                var userJob: Job? = null
                var deviceInfoJob: Job? = null
                auth.authStates
                    .collect { authState ->
                        when (authState) {
                            is AuthState.UnAuthenticated -> {
                                analytics.setUserId(null)
                                userJob?.cancel()
                                deviceInfoJob?.cancel()
                                sendSession(
                                    session.copy(
                                        authState = SessionAuthState.UnAuthenticated
                                    )
                                )
                            }
                            is AuthState.Authenticated -> {
                                analytics.setUserId(authState.authUser.uid)
                                val state = if (authState.authUser.isAnonymous) {
                                    SessionAuthState.Authenticated.Anonymous(
                                        authState.authUser.uid,
                                        authState.authToken?.token
                                    )
                                } else {
                                    SessionAuthState.Authenticated.User(
                                        authState.authUser.uid,
                                        authState.authToken?.token,
                                        null
                                    )
                                }
                                sendSession(session.copy(authState = state))

                                userJob?.cancel()
                                if (state is SessionAuthState.Authenticated.User) {
                                    userJob = launch {
                                        var currentUser = store.user(authState.authUser.uid, true)

                                        if (currentUser == null) {
                                            currentUser = store.user(authState.authUser.uid).first()
                                        }
                                        sendSession(
                                            session.copy(
                                                authState = (session.authState as SessionAuthState.Authenticated.User).copy(
                                                    user = currentUser
                                                )
                                            )
                                        )
                                    }
                                }

                                deviceInfoJob?.cancel()
                                deviceInfoJob = launch {
                                    val localDeviceInfo = deviceInfoProvider.getDeviceInfo()
                                    var deviceInfoStored =
                                        store.deviceInfo(authState.authUser.uid, localDeviceInfo.instanceId, true)

                                    sendSession(session.copy(device = localDeviceInfo))
                                    if (localDeviceInfo != deviceInfoStored) {
                                        store.saveDeviceInfo(authState.authUser.uid, localDeviceInfo)
                                    }
                                }
                            }
                        }
                    }
            }

            launch {
                val deviceInfo = deviceInfoProvider.getDeviceInfo()
                sendSession(session.copy(device = deviceInfo))
            }

            launch {
                var storeTokenJob: Job? = null
                _newTokens.consumeEach {
                    val currentSession = session
                    if (currentSession.device?.token != it &&
                        currentSession.device?.instanceId != null &&
                        currentSession.authState is SessionAuthState.Authenticated
                    ) {
                        storeTokenJob?.cancel()
                        storeTokenJob = launch {
                            val deviceInfo = DeviceInfo(currentSession.device.instanceId, it, currentSession.device.languageTag)
                            store.saveDeviceInfo(currentSession.authState.uId, deviceInfo)
                            sendSession(session.copy(device = deviceInfo))
                        }
                    }
                }
            }
        }
    }

    fun onNewDeviceIdToken(token: String) {
        logger.info { "New token received: $token" }
        onNewTokens(token)
    }
}

data class Session(
    val authState: SessionAuthState = SessionAuthState.Unknown,
    val device: DeviceInfo? = null
) {
    val isAuthenticated = authState is SessionAuthState.Authenticated
    val isAuthWithUser = authState is SessionAuthState.Authenticated.User && authState.user != null
    val hasAuthToken = authState is SessionAuthState.Authenticated && authState.authToken != null
}

sealed class SessionAuthState {
    object Unknown : SessionAuthState()
    object UnAuthenticated : SessionAuthState()
    sealed class Authenticated(open val uId: String, open val authToken: String?) : SessionAuthState() {
        data class Anonymous(
            override val uId: String,
            override val authToken: String?
        ) : Authenticated(uId, authToken)

        data class User(
            override val uId: String,
            override val authToken: String?,
            val user: com.nicolasmilliard.socialcats.store.User?
        ) : Authenticated(uId, authToken)
    }
}

interface DeviceInfoProvider {
    suspend fun getDeviceInfo(): DeviceInfo
}

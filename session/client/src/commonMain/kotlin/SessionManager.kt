package com.nicolasmilliard.socialcats.session

import com.nicolasmilliard.socialcats.auth.Auth
import com.nicolasmilliard.socialcats.auth.AuthState
import com.nicolasmilliard.socialcats.store.DeviceInfo
import com.nicolasmilliard.socialcats.store.UserStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class SessionManager(
    private val auth: Auth,
    private val store: UserStore,
    private val deviceInfoProvider: DeviceInfoProvider
) {

    private val _sessions = MutableStateFlow(Session())
    val sessions: StateFlow<Session> get() = _sessions

    private val _newTokens = Channel<String>(Channel.CONFLATED)
    val onNewTokens: (String) -> Unit get() = { _newTokens.offer(it) }

    suspend fun start() = coroutineScope {
        logger.info { "start" }

        fun sendSession(newSession: Session) {
            logger.info {
                "Sending session, $newSession"
            }
            _sessions.value = newSession
        }

        launch {
            var userJob: Job? = null
            var deviceInfoJob: Job? = null
            auth.authStates
                .collect { authState ->
                    val savedSession = _sessions.value

                    when (authState) {
                        is AuthState.UnAuthenticated -> {
                            userJob?.cancel()
                            deviceInfoJob?.cancel()
                            sendSession(
                                savedSession.copy(
                                    authState = SessionAuthState.UnAuthenticated
                                )
                            )
                        }
                        is AuthState.Authenticated -> {

                            val newState = if (authState.authUser.isAnonymous) {
                                SessionAuthState.Authenticated.Anonymous(
                                    authState.authUser.uid,
                                    authState.authToken?.token
                                )
                            } else {
                                val sameAuthUser = savedSession.authState is SessionAuthState.Authenticated &&
                                    authState.authUser.uid == savedSession.authState.uId
                                val user =
                                    if (savedSession.authState is SessionAuthState.Authenticated.User &&
                                        sameAuthUser
                                    ) {
                                        savedSession.authState.user
                                    } else null
                                SessionAuthState.Authenticated.User(
                                    authState.authUser.uid,
                                    authState.authToken?.token,
                                    user
                                )
                            }
                            sendSession(savedSession.copy(authState = newState))
                        }
                    }

                    val currentSession = _sessions.value

                    // Load Current user store if needed
                    val stateChangedToAuthenticatedUser = savedSession.authState !is SessionAuthState.Authenticated.User &&
                        currentSession.authState is SessionAuthState.Authenticated.User

                    val authUserChanged = savedSession.authState is SessionAuthState.Authenticated.User &&
                        currentSession.authState is SessionAuthState.Authenticated.User &&
                        savedSession.authState.uId != currentSession.authState.uId

                    val stateChangedFromAuthenticatedUser = savedSession.authState is SessionAuthState.Authenticated.User &&
                        currentSession.authState !is SessionAuthState.Authenticated.User

                    logger.info {
                        "stateChangedToAuthenticatedUser: $stateChangedToAuthenticatedUser, " +
                            "authUserChanged: $authUserChanged, " +
                            "stateChangedFromAuthenticatedUser $stateChangedFromAuthenticatedUser"
                    }

                    if (stateChangedToAuthenticatedUser || authUserChanged) {
                        logger.info { "Auth user changed or authenticated, loading store user" }
                        val uId = (currentSession.authState as SessionAuthState.Authenticated).uId
                        userJob?.cancel()
                        userJob = launch {
                            // TODO is this too connectivity intense?
                            store.user(uId).collect {
                                sendSession(
                                    _sessions.value.copy(
                                        authState = (_sessions.value.authState as SessionAuthState.Authenticated.User)
                                            .copy(user = it)
                                    )
                                )
                            }
                        }
                    } else if (stateChangedFromAuthenticatedUser) {
                        userJob?.cancel()
                    }

                    // save device info if needed
                    val stateChangedToAuthenticated = savedSession.authState !is SessionAuthState.Authenticated &&
                        currentSession.authState is SessionAuthState.Authenticated
                    val stateChangedToFromAuthenticated = savedSession.authState is SessionAuthState.Authenticated &&
                        currentSession.authState !is SessionAuthState.Authenticated

                    logger.info {
                        "stateChangedToAuthenticated: $stateChangedToAuthenticated. " +
                            "stateChangedToFromAuthenticated: $stateChangedToFromAuthenticated "
                    }

                    if (stateChangedToAuthenticated) {
                        logger.info { "Authentication detected, checking store device info" }
                        val uId = (currentSession.authState as SessionAuthState.Authenticated).uId
                        deviceInfoJob = launch {
                            val localDeviceInfo = deviceInfoProvider.getDeviceInfo()
                            var deviceInfoStored =
                                store.deviceInfo(uId, localDeviceInfo.instanceId, true)

                            if (localDeviceInfo != deviceInfoStored) {
                                store.saveDeviceInfo(uId, localDeviceInfo)
                            }
                        }
                    } else if (stateChangedToFromAuthenticated) {
                        deviceInfoJob?.cancel()
                    }
                }
        }

        launch {
            val deviceInfo = deviceInfoProvider.getDeviceInfo()
            sendSession(_sessions.value.copy(device = deviceInfo))
        }

        launch {
            var storeTokenJob: Job? = null
            _newTokens.consumeEach {
                val currentSession = _sessions.value
                if (currentSession.device?.token != it &&
                    currentSession.device?.instanceId != null &&
                    currentSession.authState is SessionAuthState.Authenticated
                ) {
                    storeTokenJob?.cancel()
                    storeTokenJob = launch {
                        val deviceInfo = DeviceInfo(currentSession.device.instanceId, it, currentSession.device.languageTag)
                        store.saveDeviceInfo(currentSession.authState.uId, deviceInfo)
                        sendSession(_sessions.value.copy(device = deviceInfo))
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

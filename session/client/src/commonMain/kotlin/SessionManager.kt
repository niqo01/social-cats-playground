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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class SessionManager(
    private val auth: Auth,
    private val store: SocialCatsStore,
    private val deviceInfoProvider: DeviceInfoProvider
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
                        " anonymous: ${session.authData?.isAnonymous}, Device: ${session.device}"
                }
                _sessions.offer(session)
            }

            launch {
                var userJob: Job? = null
                var deviceInfoJob: Job? = null
                auth.authStates
                    .collect { authState ->
                        when (authState) {
                            is UnAuthenticated -> {
                                userJob?.cancel()
                                deviceInfoJob?.cancel()
                                sendSession(
                                    session.copy(
                                        authData = null
                                    )
                                )
                            }
                            is Authenticated -> {
                                sendSession(
                                    session.copy(
                                        authData = AuthData(
                                            authState.authUser.uid,
                                            authState.authToken?.token,
                                            authState.authUser.isAnonymous,
                                            null
                                        )
                                    )
                                )

                                userJob?.cancel()
                                userJob = launch {
                                    var currentUser: User? = null
                                    try {
                                        currentUser = store.getCurrentUser(authState.authUser.uid, true)
                                    } catch (e: Exception) {
                                        // TODO Catch the proper exception type FirebaseFirestoreException
                                        logger.info(e) { "No stored user yet" }
                                    }
                                    if (currentUser == null) {
                                        currentUser = store.getCurrentUser(authState.authUser.uid).first()
                                    }
                                    sendSession(session.copy(authData = session.authData!!.copy(user = currentUser)))
                                }

                                deviceInfoJob?.cancel()
                                deviceInfoJob = launch {
                                    val localDeviceInfo = deviceInfoProvider.getDeviceInfo()
                                    var deviceInfoStored: DeviceInfo? = null
                                    try {
                                        deviceInfoStored =
                                            store.getDeviceInfo(authState.authUser.uid, localDeviceInfo.instanceId, true)
                                    } catch (e: Exception) {
                                        // TODO Catch the proper exception type FirebaseFirestoreException
                                        logger.info(e) { "No stored deviceInfo yet" }
                                    }

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
                    if (session.device?.token != it &&
                        session.device?.instanceId != null
                    ) {
                        storeTokenJob?.cancel()
                        storeTokenJob = launch {
                            val deviceInfo = DeviceInfo(session.device!!.instanceId, it, session.device!!.languageTag)
                            store.saveDeviceInfo(session.authData!!.uId, deviceInfo)
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
    val authData: AuthData? = null,
    val device: DeviceInfo? = null
) {
    val isAuthenticated = authData != null
    val hasAuthToken = authData?.authToken != null
}

data class AuthData(
    val uId: String,
    val authToken: String?,
    val isAnonymous: Boolean,
    val user: User? = null
)

interface DeviceInfoProvider {
    suspend fun getDeviceInfo(): DeviceInfo
}

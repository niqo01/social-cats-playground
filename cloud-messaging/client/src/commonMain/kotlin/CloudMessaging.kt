package com.nicolasmilliard.socialcats.cloudmessaging

import com.nicolasmilliard.socialcats.session.SessionManager
import com.nicolasmilliard.socialcats.session.SessionState
import com.nicolasmilliard.socialcats.store.DeviceInfo
import com.nicolasmilliard.socialcats.store.SocialCatsStore
import com.nicolasmilliard.socialcats.test.IO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

interface InstanceIdProvider {
    suspend fun getId(): String
    val languageTag: String
}

class CloudMessaging(
    private val instanceIdProvider: InstanceIdProvider,
    private val sessionManager: SessionManager,
    private val store: SocialCatsStore
) {

    private val _tokens = ConflatedBroadcastChannel<String>()
    private val tokens = _tokens.asFlow().distinctUntilChanged()
    val offerToken: (String) -> Unit get() = { _tokens.offer(it) }

    private val _messages = BroadcastChannel<Message>(5)
    private val messages = _messages.asFlow().distinctUntilChanged()
    val offerMessage: (Message) -> Unit get() = { _messages.offer(it) }

    suspend fun start() {
        coroutineScope {
            launch(Dispatchers.IO()) {
                sessionManager.sessions
                    .filter { it is SessionState.Session }.map { it as SessionState.Session }
                    .flatMapLatest { session ->
                        tokens.map {
                            Pair(
                                session.user.id,
                                DeviceInfo(instanceIdProvider.getId(), it, instanceIdProvider.languageTag)
                            )
                        }
                    }
                    .collect {
                        store.saveInstanceId(it.first, it.second)
                    }
            }
            launch {
                messages.collect {
                    // Log message received
                }
            }
        }
    }

    fun onDeletedMessages() {
        TODO()
    }
}

private data class UserDevice(
    val userId: String,
    val deviceId: String,
    val token: String
)

data class Message(
    val messageId: String?,
    val messageType: String?,
    val collapseKey: String?,
    val sentTime: Long,
    val data: Map<String, String>,
    val notification: Notification?
)

data class Notification(
    val body: String?,
    val title: String?,
    val channelId: String?
)

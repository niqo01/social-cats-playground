package com.nicolasmilliard.socialcats.cloudmessaging

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}
class CloudMessaging {

    private val _messages = BroadcastChannel<Message>(5)
    val messages = _messages.asFlow().distinctUntilChanged()
    val offerMessage: (Message) -> Unit get() = { _messages.offer(it) }

    suspend fun start() = coroutineScope {
        messages.collect {
            logger.info { "CloudMessaging message received: $it" }
        }
    }

    fun onDeletedMessages() {
        TODO()
    }
}

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

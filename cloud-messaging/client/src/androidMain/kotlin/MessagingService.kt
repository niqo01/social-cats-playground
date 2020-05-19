package com.nicolasmilliard.socialcats.cloudmessaging

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.nicolasmilliard.socialcats.session.SessionManager
import com.nicolasmilliard.socialcats.session.sessionManager
import org.koin.android.ext.android.inject

class MessagingService : FirebaseMessagingService() {

    private val cloudMessaging: CloudMessaging by inject()
    private val sessionManager: SessionManager by inject()

    override fun onNewToken(token: String) {
        sessionManager.onNewDeviceIdToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        cloudMessaging.offerMessage(message.toMessage())
    }

    override fun onDeletedMessages() {
        TODO()
    }

    private fun RemoteMessage.Notification.toNotification() = Notification(body, title, channelId)
    private fun RemoteMessage.toMessage() =
        Message(messageId, messageType, collapseKey, sentTime, data, notification?.toNotification())
}

package com.nicolasmilliard.socialcats.cloudmessaging

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.nicolasmilliard.socialcats.session.SessionManager
import com.nicolasmilliard.socialcats.session.sessionManager

class MessagingService : FirebaseMessagingService() {

    private lateinit var cloudMessaging: CloudMessaging
    private lateinit var sessionManager: SessionManager

    override fun onCreate() {
        super.onCreate()
        cloudMessaging = applicationContext.cloudMessaging
        sessionManager = applicationContext.sessionManager
    }

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

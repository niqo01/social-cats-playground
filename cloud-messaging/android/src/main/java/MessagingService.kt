package com.nicolasmilliard.socialcats.cloudmessaging.android

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.nicolasmilliard.socialcats.cloudmessaging.CloudMessaging
import com.nicolasmilliard.socialcats.cloudmessaging.Message
import com.nicolasmilliard.socialcats.cloudmessaging.Notification
import com.nicolasmilliard.socialcats.component
import com.nicolasmilliard.socialcats.session.SessionManager

class MessagingService : FirebaseMessagingService() {

    private lateinit var cloudMessaging: CloudMessaging
    private lateinit var sessionManager: SessionManager

    override fun onCreate() {
        super.onCreate()
        cloudMessaging = applicationContext.component.cloudMessaging
        sessionManager = applicationContext.component.sessionManager
    }

    override fun onNewToken(token: String) {
        sessionManager.onNewToken(token)
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

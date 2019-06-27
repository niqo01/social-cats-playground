package com.nicolasmilliard.socialcats.cloudmessaging

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.nicolasmilliard.socialcats.component

class MessagingService : FirebaseMessagingService() {

    private lateinit var cloudMessaging: CloudMessaging

    override fun onCreate() {
        super.onCreate()
        cloudMessaging = applicationContext.component.cloudMessaging
    }

    override fun onNewToken(token: String) {
        cloudMessaging.offerToken(token)
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

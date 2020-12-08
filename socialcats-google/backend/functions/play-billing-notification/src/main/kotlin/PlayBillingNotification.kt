package com.nicolasmilliard.socialcats.auth

import com.google.cloud.functions.Context
import com.google.cloud.functions.RawBackgroundFunction
import com.squareup.moshi.Moshi
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

private val log = KotlinLogging.logger {}

/**
 * Function triggered on new Auth user (/databases/(default)/documents/users/{id}) modifications.
 */
class PlayBillingNotification(graph: Graph = AppComponent().build()) : RawBackgroundFunction {

    private val messageConverter = PubSubConverter(graph.moshi)
    private val newUserHandler = graph.newUserHandler

    init {
        graph.appInitializer.initialize()
    }

    override fun accept(json: String, context: Context): Unit = runBlocking {
        try {
            log.debug {
                "Event ID: ${context.eventId()}, Resource: ${context.resource()}," +
                    " Event Type: ${context.eventType()}, Timestamp: ${context.timestamp()}"
            }
            log.debug { "json: $json" }

            val pubSubMessage = messageConverter.convert(json)
            log.debug { "Message received: $pubSubMessage" }

            //
            pubSubMessage.data?.subscriptionNotification
        } catch (e: Throwable) {
            log.error(e) { "Error while processing event" }
            throw e
        }
    }

    private class PubSubConverter(moshi: Moshi) {

        private val adapter = moshi.adapter(PubSubMessage::class.java)

        fun convert(json: String): PubSubMessage {
            return checkNotNull(adapter.fromJson(json))
        }
    }
}

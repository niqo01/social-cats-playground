package com.nicolasmilliard.socialcats

import com.google.cloud.functions.Context
import com.google.cloud.functions.RawBackgroundFunction
import com.nicolasmilliard.socialcats.store.DbConstants
import com.squareup.moshi.JsonAdapter
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Function triggered on Firestore documents (/databases/(default)/documents/users/{id}) modifications.
 */
class FirestoreUserChangedFunction(
    graph: Graph = AppComponent().build()
) : RawBackgroundFunction {

    private val moshi = graph.moshi
    private val searchUseCase = graph.searchUseCase
    private val payments = graph.stripePayments

    override fun accept(json: String, context: Context) = runBlocking {
        try {
            logger.debug {
                "Event ID: ${context.eventId()}, Resource: ${context.resource()}, " +
                    "Event Type: ${context.eventType()}, Timestamp: ${context.timestamp()}\n" +
                    "Json content: $json"
            }

            val jsonAdapter: JsonAdapter<FirestoreEvent> = moshi.adapter(FirestoreEvent::class.java)
            val event: FirestoreEvent = checkNotNull(jsonAdapter.fromJson(json))

            logger.info { "On User written, Event: $event" }

            when (event) {
                is CreatedEvent -> {
                    val asyncIndex = async { searchUseCase.indexUser(event.value) }
                    val asyncPayment = async {
                        payments.value.createCustomer(
                            event.value.resourceId,
                            event.value.fields[DbConstants.Collections.Users.Fields.EMAIL] as String?,
                            event.value.fields[DbConstants.Collections.Users.Fields.PHONE_NUMBER] as String?
                        )
                    }
                    try {
                        asyncIndex.await()
                    } catch (e: Exception) {
                        logger.error(e) { "Failed to Index new User" }
                        // TODO Try Google cloud task
                    }
                    try {
                        asyncPayment.await()
                    } catch (e: Exception) {
                        logger.error(e) { "Failed to create Payment customer" }
                        // TODO Try Google cloud task
                    }
                }
                is UpdatedEvent -> searchUseCase.indexUser(event.value)
                is DeletedEvent -> searchUseCase.deleteUser(event.resourceId)
            }
        } catch (e: Throwable) {
            logger.error(e) { "Error while processing event" }
            throw e
        }
    }
}

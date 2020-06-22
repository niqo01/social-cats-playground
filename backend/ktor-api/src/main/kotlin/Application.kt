package com.nicolasmilliard.socialcats.searchapi

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.nicolasmilliard.socialcats.api.ApiError
import com.nicolasmilliard.socialcats.api.ApiErrors
import com.nicolasmilliard.socialcats.api.ERROR_CATEGORY_TECHNICAL
import com.nicolasmilliard.socialcats.api.ERROR_CODE_INTERNAL_SERVER_ERROR
import com.nicolasmilliard.socialcats.payment.StripePayments
import com.nicolasmilliard.socialcats.search.SearchUseCase
import com.nicolasmilliard.socialcats.searchapi.routes.dummySearch2
import com.nicolasmilliard.socialcats.searchapi.routes.home
import com.nicolasmilliard.socialcats.searchapi.routes.paymentCancelSubscription
import com.nicolasmilliard.socialcats.searchapi.routes.paymentCreateCheckoutSession
import com.nicolasmilliard.socialcats.searchapi.routes.paymentCreateSubscription
import com.nicolasmilliard.socialcats.searchapi.routes.paymentSubscriptionDetail
import com.nicolasmilliard.socialcats.searchapi.routes.search
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationStopped
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.features.CallId
import io.ktor.features.CallLogging
import io.ktor.features.Compression
import io.ktor.features.ConditionalHeaders
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.features.callIdMdc
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Locations
import io.ktor.request.header
import io.ktor.response.respond
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.server.engine.ShutDownUrl
import kotlinx.atomicfu.atomic
import org.elasticsearch.client.RestHighLevelClient
import org.koin.core.module.Module
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.inject
import org.slf4j.event.Level

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(modules: List<Module> = getModules(environment.config)) {

    val initFirebaseApp = environment.config
        .property("google.initFirebaseApp").getString() == "true"
    if (initFirebaseApp) {
        val projectId = environment.config.property("google.projectId").getString()
        initFirebaseApp(projectId)
    }

    install(Koin) {
        modules(modules)
    }

    val esClient: RestHighLevelClient by inject()

    environment.monitor.subscribe(ApplicationStopped) {
        println("Time to clean up")
        esClient.close()
    }

    install(CallLogging) {
        level = Level.TRACE
        callIdMdc("mdc-call-id")
    }

    install(DefaultHeaders)
    install(ShutDownUrl.ApplicationCallFeature) {
        // The URL that will be intercepted (you can also use the application.conf's ktor.deployment.shutdown.url key)
        shutDownUrl = "/_ah/stop"
        // A function that will be executed to get the exit code of the process
        exitCodeSupplier = { 0 } // ApplicationCall.() -> Int
    }

    val tokenVerifier: FirebaseTokenVerifier by inject()
    install(Authentication) {
        firebaseAuth(FirebaseAuthKey) {
            firebaseTokenVerifier = tokenVerifier
            validate = { PrincipalToken(it) }
        }
    }
    install(ContentNegotiation) {
        json()
    }
    install(Locations)
    install(CallId) {
        retrieve { call: ApplicationCall ->
            call.request.header(HttpHeaders.XRequestId)
        }

        val counter = atomic(0)
        generate { "generated-call-id-${counter.getAndIncrement()}" }

        // Once a callId is generated, this optional function is called to verify if the retrieved or generated callId String is valid.
        verify { callId: String ->
            callId.isNotEmpty()
        }
    }

    install(Compression)
    install(ConditionalHeaders)
    install(StatusPages) {
        exception<Throwable> { cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiErrors(listOf(ApiError(ERROR_CATEGORY_TECHNICAL, ERROR_CODE_INTERNAL_SERVER_ERROR)))
            )
            throw cause
        }
    }

    val searchUseCase: SearchUseCase by inject()
    val stripePayments: StripePayments by inject()

    routing {
        home()
        search(searchUseCase)
        paymentCreateCheckoutSession(stripePayments)
        paymentSubscriptionDetail(stripePayments)
        paymentCreateSubscription(stripePayments)
        paymentCancelSubscription(stripePayments)
        dummySearch2(searchUseCase)
    }
}

fun initFirebaseApp(projectId: String) {
    val credentials = GoogleCredentials.getApplicationDefault()
    val options = FirebaseOptions.Builder()
        .setCredentials(credentials)
        .setProjectId(projectId)
        .build()
    FirebaseApp.initializeApp(options)
}

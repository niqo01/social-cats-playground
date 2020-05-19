package com.nicolasmilliard.socialcats.searchapi

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.nicolasmilliard.socialcats.search.SearchUseCase
import com.nicolasmilliard.socialcats.searchapi.routes.dummySearch2
import com.nicolasmilliard.socialcats.searchapi.routes.home
import com.nicolasmilliard.socialcats.searchapi.routes.search
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationStopped
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.features.CallId
import io.ktor.features.CallLogging
import io.ktor.features.Compression
import io.ktor.features.ConditionalHeaders
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.callIdMdc
import io.ktor.http.HttpHeaders
import io.ktor.locations.Locations
import io.ktor.request.header
import io.ktor.request.path
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.server.engine.ShutDownUrl
import kotlinx.atomicfu.atomic
import org.elasticsearch.client.RestHighLevelClient
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.inject
import org.slf4j.event.Level

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    if (!testing) {
        val projectId = environment.config.property("google.projectId").getString()
        initFirebaseApp(projectId)
    }

    install(Koin) {
        modules(getModules(environment.config, testing))
    }

    val esClient: RestHighLevelClient by inject()

    environment.monitor.subscribe(ApplicationStopped) {
        println("Time to clean up")
        esClient.close()
    }

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
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

    val searchUseCase: SearchUseCase by inject()

    routing {
        home()
        search(searchUseCase)
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

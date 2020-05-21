package com.nicolasmilliard.socialcats.searchapi

import com.google.firebase.auth.FirebaseAuthException
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.auth.Authentication
import io.ktor.auth.AuthenticationContext
import io.ktor.auth.AuthenticationFailedCause
import io.ktor.auth.AuthenticationPipeline
import io.ktor.auth.AuthenticationProvider
import io.ktor.auth.Principal
import io.ktor.auth.UnauthorizedResponse
import io.ktor.auth.parseAuthorizationHeader
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.response.respond
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val Logger: Logger =
    LoggerFactory.getLogger("com.nicolasmilliard.socialcats.searchapi.firebaseAuth")

const val FirebaseAuthKey = "FirebaseAuth"
private const val scheme = "Bearer"

data class PrincipalToken(val value: Token) : Principal

data class Token(
    val uid: String,
    val issuer: String? = null,
    val name: String? = null,
    val picture: String? = null,
    val email: String? = null,
    val isEmailVerified: Boolean,
    val claims: Map<String, Any>
)

interface FirebaseTokenVerifier {
    fun verifyIdToken(token: String): Token
}

class FirebaseAuthProvider internal constructor(
    config: Configuration
) : AuthenticationProvider(config) {

    private val firebaseAuth = config.firebaseTokenVerifier

    private var validateFunc = config.validate

    class Configuration internal constructor(name: String?) :
        AuthenticationProvider.Configuration(name) {
        lateinit var firebaseTokenVerifier: FirebaseTokenVerifier
        lateinit var validate: suspend ApplicationCall.(Token) -> Principal?
        internal fun build() = FirebaseAuthProvider(this)
    }

    suspend fun verifyIdToken(call: ApplicationCall, tokenStr: String): Principal? =
        withContext(Dispatchers.IO) {
            try {
                val firebaseToken = firebaseAuth.verifyIdToken(tokenStr)
                call.validateFunc(firebaseToken)
            } catch (e: Throwable) {
                call.application.log.warn("Verifying token failed", e)
                throw e
            }
        }
}

/**
 * FirebaseToken
 *
 * @param name [FirebaseAuthProvider]
 */
fun Authentication.Configuration.firebaseAuth(
    name: String = "",
    configure: FirebaseAuthProvider.Configuration.() -> Unit
) {
    val provider = FirebaseAuthProvider.Configuration(name).apply(configure).build()
    provider.auth()
    register(provider)
}

internal fun FirebaseAuthProvider.auth() {
    pipeline.intercept(AuthenticationPipeline.RequestAuthentication) { context ->
        val authHeader = call.request.parseAuthorizationHeader()
        if (authHeader == null) {
            context.bearerChallenge(AuthenticationFailedCause.NoCredentials)
            return@intercept
        }

        val principal = try {
            authHeader.bearerBlob?.let { verifyIdToken(call, it) }
        } catch (cause: FirebaseAuthException) {
            Logger.trace("Firebase Auth verifying token failed", cause)
            null
        } catch (cause: Throwable) {
            Logger.error("Firebase Auth unknown error", cause)
            context.error(
                FirebaseAuthKey,
                AuthenticationFailedCause.Error("Failed to verify auth token due to $cause")
            )
            null
        }

        if (principal != null) {
            context.principal(principal)
        } else {
            context.bearerChallenge(AuthenticationFailedCause.InvalidCredentials)
        }
    }
}

private fun AuthenticationContext.bearerChallenge(
    failedCause: AuthenticationFailedCause
) = challenge(FirebaseAuthKey, failedCause) {
    call.respond(UnauthorizedResponse(HttpAuthHeader.bearerAuthChallenge))
    it.complete()
}

private val HttpAuthHeader.Companion.bearerAuthChallenge: HttpAuthHeader.Parameterized
    get() =
        HttpAuthHeader.Parameterized(scheme, mapOf())

private val HttpAuthHeader.bearerBlob: String?
    get() = when {
        this is HttpAuthHeader.Single && authScheme.toLowerCase() == scheme.toLowerCase() -> blob
        else -> null
    }

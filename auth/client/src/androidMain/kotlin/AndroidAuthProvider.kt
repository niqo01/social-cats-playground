package com.nicolasmilliard.socialcats.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseAuth.IdTokenListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class AndroidAuthProvider(private val firebaseAuth: FirebaseAuth) : AuthProvider {

    override suspend fun signInWithCredential(credential: AuthCredential) {
        firebaseAuth.signInWithCredential(credential).await()
    }

    override suspend fun signInAnonymously() {
        firebaseAuth.signInAnonymously().await()
    }

    override suspend fun linkWithGoogleCredentials(googleIdToken: String) {
        val currentUser = firebaseAuth.currentUser
        checkNotNull(currentUser)
        val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
        currentUser.linkWithCredential(credential).await()
    }

    override fun getAuthUser(): Flow<AuthUser?> = callbackFlow {
        val listener = AuthStateListener {
            val currentUser = it.currentUser
            logger.info { "AuthState: $currentUser, id: ${currentUser?.uid}, ${currentUser?.isAnonymous}" }
            offer(currentUser)
        }
        firebaseAuth.addAuthStateListener(listener)

        offer(firebaseAuth.currentUser)
        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }.map {
        it?.toAuthUser()
    }.distinctUntilChanged().conflate()

    override fun getAuthToken(): Flow<NewToken?> = callbackFlow {
        val listener = IdTokenListener {
            launch {
                val currentUser = it.currentUser
                logger.info { "AuthState (Token): $currentUser, id: ${currentUser?.uid}, ${currentUser?.isAnonymous}" }
                if (currentUser == null) {
                    offer(null)
                } else {
                    val token = currentUser.getIdToken(false).await().token
                    offer(NewToken(token?.toAuthToken(), currentUser.toAuthUser()))
                }
            }
        }
        firebaseAuth.addIdTokenListener(listener)

        launch {
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                val token = firebaseAuth.currentUser?.getIdToken(false)?.await().token
                offer(NewToken(token?.toAuthToken(), currentUser.toAuthUser()))
            }
        }
        awaitClose {
            firebaseAuth.removeIdTokenListener(listener)
        }
    }
        .distinctUntilChanged().conflate()
}

private fun String.toAuthToken() = AuthToken(this)
private fun FirebaseUser.toAuthUser() = AuthUser(
    this.uid,
    this.isAnonymous,
    this.displayName,
    this.photoUrl.toString(),
    this.email,
    this.phoneNumber
)

actual typealias AuthCredential = com.google.firebase.auth.AuthCredential

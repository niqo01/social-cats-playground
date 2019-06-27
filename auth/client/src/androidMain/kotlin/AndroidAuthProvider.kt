package com.nicolasmilliard.socialcats.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseAuth.IdTokenListener
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
            offer(currentUser)
        }
        firebaseAuth.addAuthStateListener(listener)

        offer(firebaseAuth.currentUser)
        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }.map {
        if (it == null) null else AuthUser(
            it.uid,
            it.isAnonymous,
            it.displayName,
            it.photoUrl.toString(),
            it.email,
            it.phoneNumber
        )
    }.distinctUntilChanged().conflate()

    override fun getAuthToken(): Flow<AuthToken?> = callbackFlow {
        val listener = IdTokenListener {
            launch {
                val currentUser = it.currentUser
                if (currentUser == null) {
                    offer(null)
                } else {
                    val token = currentUser.getIdToken(false).await().token
                    offer(token)
                }
            }
        }
        firebaseAuth.addIdTokenListener(listener)

        launch {
            offer(firebaseAuth.currentUser?.getIdToken(false)?.await().token)
        }
        awaitClose {
            firebaseAuth.removeIdTokenListener(listener)
        }
    }.map { if (it == null) null else AuthToken(it) }
        .distinctUntilChanged().conflate()
}

actual typealias AuthCredential = com.google.firebase.auth.AuthCredential

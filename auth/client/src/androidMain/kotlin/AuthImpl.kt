package com.nicolasmilliard.socialcats.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseAuth.IdTokenListener
import com.google.firebase.auth.GoogleAuthProvider
import com.nicolasmilliard.socialcats.auth.AuthState.Authenticated
import com.nicolasmilliard.socialcats.auth.AuthState.UnAuthenticated
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthImpl(private val firebaseAuth: FirebaseAuth) : Auth {

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

    override fun getAuthState(): Flow<AuthState> {
        return getFirebaseUser()
            .flatMapLatest { authUser ->
                if (authUser is UnAuthenticated) {
                    flowOf(authUser)
                } else {
                    getAuthToken().map {
                        if (it == null) {
                            UnAuthenticated
                        } else {
                            Authenticated(it, (authUser as Authenticated).authUser)
                        }
                    }.onStart { emit(authUser) }
                }
            }.distinctUntilChanged()
    }

    private fun getFirebaseUser(): Flow<AuthState> = callbackFlow {
        val listener = AuthStateListener {
            val currentUser = it.currentUser
            if (currentUser == null) {
                offer(UnAuthenticated)
            } else {
                launch {
                    val token = currentUser.getIdToken(false).await().token
                    if (token == null) {
                        offer(UnAuthenticated)
                    } else {
                        offer(
                            Authenticated(
                                AuthToken(token),
                                AuthUser(
                                    currentUser.uid,
                                    currentUser.isAnonymous,
                                    currentUser.displayName,
                                    currentUser.photoUrl.toString(),
                                    currentUser.email,
                                    currentUser.phoneNumber
                                )
                            )
                        )
                    }
                }
            }
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }.conflate()

    private fun getAuthToken(): Flow<AuthToken?> = callbackFlow {
        val listener = IdTokenListener {
            launch {
                val currentUser = it.currentUser
                if (currentUser == null) {
                    offer(null)
                } else {
                    val token = currentUser.getIdToken(false).await().token
                    if (token == null) {
                        offer(null)
                    } else {
                        offer(AuthToken(token))
                    }
                }
            }
        }
        firebaseAuth.addIdTokenListener(listener)
        awaitClose {
            firebaseAuth.removeIdTokenListener(listener)
        }
    }.conflate()
}

actual typealias AuthCredential = com.google.firebase.auth.AuthCredential

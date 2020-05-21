package com.nicolasmilliard.socialcats.auth.ui

import android.content.Context
import android.content.Intent
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.nicolasmilliard.socialcats.auth.DeleteStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

val PROVIDERS = listOf(IdpConfig.PhoneBuilder().build(), IdpConfig.GoogleBuilder().build())
const val SIGN_IN_REQUEST_CODE = 6666
const val RE_AUTH_REQUEST_CODE = 6667

class AndroidAuthUi(
    private val context: Context,
    private val authUI: AuthUI
) : AuthUi {

    override suspend fun signOut(): Unit = withContext(Dispatchers.IO) {
        authUI.signOut(context).await()
    }

    override suspend fun silentSignIn(): Unit = withContext(Dispatchers.IO) {
        authUI.silentSignIn(context, PROVIDERS).await()
    }

    override suspend fun delete(): DeleteStatus = withContext(Dispatchers.IO) {
        try {
            authUI.delete(context).await()
            DeleteStatus.SUCCESS
        } catch (e: FirebaseAuthRecentLoginRequiredException) {
            DeleteStatus.AUTH_RECENT_LOGIN_REQUIRED
        }
    }

    fun createSignInIntent(): Intent {
        return authUI.createSignInIntentBuilder()
            .enableAnonymousUsersAutoUpgrade()
            .setAvailableProviders(PROVIDERS)
            .build()
    }

    fun createReAuthIntent(): Intent {
        return authUI.createSignInIntentBuilder()
            .setAvailableProviders(PROVIDERS)
            .build()
    }
}

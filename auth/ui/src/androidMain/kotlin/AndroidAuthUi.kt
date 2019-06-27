package com.nicolasmilliard.socialcats.auth.ui

import android.content.Context
import android.content.Intent
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig
import kotlinx.coroutines.tasks.await

val PROVIDERS = listOf(IdpConfig.PhoneBuilder().build(), IdpConfig.GoogleBuilder().build())
const val SIGN_IN_REQUEST_CODE = 6666

class AndroidAuthUi(
    private val context: Context,
    private val authUI: AuthUI
) : AuthUi {

    override suspend fun signOut() {
        authUI.signOut(context).await()
    }

    override suspend fun silentSignIn() {
        authUI.silentSignIn(context, PROVIDERS).await()
    }

    override suspend fun delete() {
        authUI.delete(context).await()
    }

    fun createSignInIntent(): Intent {
        return authUI.createSignInIntentBuilder()
            .enableAnonymousUsersAutoUpgrade()
            .setAvailableProviders(PROVIDERS)
            .build()
    }
}

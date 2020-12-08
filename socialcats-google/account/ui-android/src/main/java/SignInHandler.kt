package com.nicolasmilliard.socialcats.account.ui

import android.app.Activity
import com.nicolasmilliard.socialcats.auth.ui.AndroidAuthUi
import com.nicolasmilliard.socialcats.auth.ui.SIGN_IN_REQUEST_CODE

class SignInHandler(private val activity: Activity, private val authUi: AndroidAuthUi) {
    operator fun invoke() {
        // not signed in
        activity.startActivityForResult(
            // Get an instance of AuthUI based on the default app
            authUi.createSignInIntent(),
            SIGN_IN_REQUEST_CODE
        )
    }
}

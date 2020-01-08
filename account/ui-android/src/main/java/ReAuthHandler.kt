package com.nicolasmilliard.socialcats.account.ui

import android.app.Activity
import com.nicolasmilliard.socialcats.auth.ui.AndroidAuthUi
import com.nicolasmilliard.socialcats.auth.ui.RE_AUTH_REQUEST_CODE

class ReAuthHandler(private val activity: Activity, private val authUi: AndroidAuthUi) {
    operator fun invoke() {
        // not signed in
        activity.startActivityForResult(
            // Get an instance of AuthUI based on the default app
            authUi.createReAuthIntent(),
            RE_AUTH_REQUEST_CODE
        )
    }
}

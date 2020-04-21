package com.nicolasmilliard.socialcats.bugreporter

import com.google.firebase.crashlytics.FirebaseCrashlytics

class PlatformBugReporter : BugReporter {

    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun log(message: String) {
        crashlytics.log(message)
    }

    override fun setUserId(id: String?) {
        crashlytics.setUserId(id ?: "") // see  https://github.com/firebase/firebase-android-sdk/issues/1512
    }

    override fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }
}

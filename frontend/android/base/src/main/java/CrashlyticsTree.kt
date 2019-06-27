package com.nicolasmilliard.socialcats

import com.crashlytics.android.Crashlytics
import timber.log.Timber.Tree

class CrashlyticsTree : Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        Crashlytics.log(priority, tag, message)
        if (t != null) {
            Crashlytics.logException(t)
        }
    }
}

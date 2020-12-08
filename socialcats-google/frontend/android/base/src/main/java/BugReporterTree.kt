package com.nicolasmilliard.socialcats

import com.nicolasmilliard.socialcats.bugreporter.BugReporter
import timber.log.Timber.Tree

class BugReporterTree(val bugReporter: BugReporter) : Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        bugReporter.log("$priority - $tag - $message")
        if (t != null) {
            bugReporter.recordException(t)
        }
    }
}

package com.nicolasmilliard.socialcats.bugreporter

object BugReporterModule {

    fun provideBugReporter(): BugReporter =
        PlatformBugReporter()
}

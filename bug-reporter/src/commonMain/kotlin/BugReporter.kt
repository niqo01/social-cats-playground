package com.nicolasmilliard.socialcats.bugreporter

interface BugReporter {
    fun log(message: String)
    fun setUserId(id: String?)
    fun recordException(throwable: Throwable)
}

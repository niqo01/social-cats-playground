package com.nicolasmilliard.socialcats.analytics

interface Analytics {
    fun logEvent(name: String, params: Map<String, String>? = null)
    fun setUserId(id: String?)
    fun setUserProperty(name: String, value: String?)
}

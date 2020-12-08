package com.nicolasmilliard.socialcats.analytics

import android.content.Context
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics

class PlatformAnalytics(context: Context) : Analytics {

    private val firebase = FirebaseAnalytics.getInstance(context)

    override fun logEvent(name: String, params: Map<String, String>?) {
        val bundle = params?.let { bundleOf(*params.toList().toTypedArray()) }
        firebase.logEvent(name, bundle)
    }

    override fun setUserId(id: String?) {
        firebase.setUserId(id)
    }

    override fun setUserProperty(name: String, value: String?) {
        firebase.setUserProperty(name, value)
    }
}

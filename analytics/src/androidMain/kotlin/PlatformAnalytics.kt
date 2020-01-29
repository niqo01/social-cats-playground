import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import com.nicolasmilliard.socialcats.analytics.Analytics

class PlatformAnalytics(private val analyticsProvider: FirebaseAnalytics) : Analytics {

    override fun logEvent(name: String, params: Map<String, String>?) {
        val bundle = params?.let { bundleOf(*params.toList().toTypedArray()) }
        analyticsProvider.logEvent(name, bundle)
    }

    override fun setUserId(id: String?) {
        analyticsProvider.setUserId(id)
    }

    override fun setUserProperty(name: String, value: String?) {
        analyticsProvider.setUserProperty(name, value)
    }
}

package com.nicolasmilliard.socialcats.analytics

import android.content.Context

object AnalyticsModule {

    fun provideAnalytics(app: Context): Analytics =
        PlatformAnalytics(app)
}

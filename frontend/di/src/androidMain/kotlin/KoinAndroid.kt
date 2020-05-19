package com.nicolasmilliard.socialcats.di

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.jakewharton.byteunits.BinaryByteUnit.MEBIBYTES
import com.nicolasmilliard.socialcats.NetworkManager
import com.nicolasmilliard.socialcats.analytics.AnalyticsModule
import com.nicolasmilliard.socialcats.auth.AndroidAuthProvider
import com.nicolasmilliard.socialcats.auth.AuthProvider
import com.nicolasmilliard.socialcats.bugreporter.BugReporterModule
import com.nicolasmilliard.socialcats.session.AndroidInstanceIdProvider
import com.nicolasmilliard.socialcats.session.DeviceInfoProvider
import com.nicolasmilliard.socialcats.store.RealUserStore
import com.nicolasmilliard.socialcats.store.UserStore
import com.nicolasmilliard.socialcats.ui.AndroidNetworkManager
import java.io.File
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY
import okhttp3.logging.HttpLoggingInterceptor.Logger
import org.koin.core.module.Module
import org.koin.dsl.module
import timber.log.Timber

actual val platformModule: Module = module {
    single {
        AnalyticsModule.provideAnalytics(get())
    }

    single {
        BugReporterModule.provideBugReporter()
    }

    single {
        val db = Firebase.firestore
        val settings = FirebaseFirestoreSettings.Builder()
            .setCacheSizeBytes(5242880L) // 5MB
            .build()
        db.firestoreSettings = settings
        db
    }

    single {
        WorkManager.getInstance(get())
    }

    single<UserStore> {
        RealUserStore(get(), get())
    }

    single<DeviceInfoProvider> {
        AndroidInstanceIdProvider()
    }

    single<AuthProvider> {
        AndroidAuthProvider(FirebaseAuth.getInstance())
    }

    single<PlatformInitializer> {
        AndroidAppInitializer(get(), get())
    }

    single<NetworkManager> {
        val context: Context = get()
        val connectivityManager = context.getSystemService<ConnectivityManager>()

        AndroidNetworkManager(connectivityManager!!)
    }

    single {
        lazy {
            val context: Context = get()
            val cacheDir = context.cacheDir / "http"

            OkHttpClient.Builder()
                .cache(Cache(cacheDir, MEBIBYTES.toBytes(10)))
                .addNetworkInterceptor(
                    HttpLoggingInterceptor(object : Logger {
                        override fun log(message: String) = Timber.d(message)
                    }).apply { level = if (BuildConfig.DEBUG) BODY else BASIC })
                .addNetworkInterceptor(UserAgentInterceptor())
                .build()
        }
    }
}

private operator fun File.div(pathSegment: String) = File(this, pathSegment)

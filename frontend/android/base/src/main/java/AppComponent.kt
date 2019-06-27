package com.nicolasmilliard.socialcats

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import androidx.work.WorkManager
import coil.ImageLoader
import coil.util.CoilUtils
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.jakewharton.byteunits.BinaryByteUnit.MEBIBYTES
import com.nicolasmilliard.socialcats.auth.AndroidAuthProvider
import com.nicolasmilliard.socialcats.auth.Auth
import com.nicolasmilliard.socialcats.auth.ui.AndroidAuthUi
import com.nicolasmilliard.socialcats.base.BuildConfig
import com.nicolasmilliard.socialcats.cloudmessaging.CloudMessaging
import com.nicolasmilliard.socialcats.http.UserAgentInterceptor
import com.nicolasmilliard.socialcats.session.AndroidInstanceIdProvider
import com.nicolasmilliard.socialcats.session.DeviceInfoProvider
import com.nicolasmilliard.socialcats.session.SessionManager
import com.nicolasmilliard.socialcats.store.RealUserStore
import com.nicolasmilliard.socialcats.store.UserStore
import com.nicolasmilliard.socialcats.ui.AndroidNetworkManager
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY
import okhttp3.logging.HttpLoggingInterceptor.Logger
import timber.log.Timber

interface AppComponentProvider {
    val appComponent: AppComponent
}

class AppComponent(val app: Application, scope: CoroutineScope) {
    val httpClient = lazy { AppModule.provideOkHttp(app) }
    val auth by lazy {
        val auth = AppModule.provideAuth()
        scope.launch {
            auth.start()
        }
        auth
    }
    val authUi = AppModule.provideAuthUi(app)
    val firestore = AppModule.provideFirestore()
    val workManager = AppModule.provideWorkManager(app)
    val store = AppModule.provideSocialCatsStore(firestore, workManager)
    val sessionManager by lazy {
        val manager = AppModule.provideSessionManager(auth, store, AndroidInstanceIdProvider())
        scope.launch {
            manager.start()
        }
        manager
    }
    val connectivityChecker by lazy {
        val checker = AppModule.provideConnectivityChecker(app)
        scope.launch {
            checker.start()
        }
        checker
    }

    val cloudMessaging by lazy {
        val cloudMessaging = AppModule.provideCloudMessaging()
        scope.launch {
            cloudMessaging.start()
        }
        cloudMessaging
    }

    val imageLoader = ImageLoader(app) {
        okHttpClient {
            httpClient.value.newBuilder()
                .cache(CoilUtils.createDefaultCache(app))
                .build()
        }
    }
}

object AppModule {

    fun provideConnectivityChecker(context: Context): ConnectivityChecker {
        val connectivityManager = context.getSystemService<ConnectivityManager>()
        requireNotNull(connectivityManager)
        return ConnectivityChecker(AndroidNetworkManager(connectivityManager))
    }

    fun provideAuthUi(context: Context): AndroidAuthUi {
        return AndroidAuthUi(context, AuthUI.getInstance())
    }

    fun provideAuth(): Auth {
        return Auth(AndroidAuthProvider(FirebaseAuth.getInstance()))
    }

    fun provideFirestore(): FirebaseFirestore {
        return Firebase.firestore
    }

    fun provideWorkManager(context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    fun provideSocialCatsStore(firestore: FirebaseFirestore, workManager: WorkManager): UserStore =
        RealUserStore(firestore, workManager)

    fun provideSessionManager(auth: Auth, store: UserStore, deviceInfoProvider: DeviceInfoProvider) =
        SessionManager(auth, store, deviceInfoProvider)

    fun provideOkHttp(application: Application): OkHttpClient {
        val cacheDir = application.cacheDir / "http"

        return OkHttpClient.Builder()
            .cache(Cache(cacheDir, MEBIBYTES.toBytes(10)))
            .addNetworkInterceptor(
                HttpLoggingInterceptor(object : Logger {
                    override fun log(message: String) = Timber.d(message)
                }).apply { level = if (BuildConfig.DEBUG) BODY else BASIC })
            .addNetworkInterceptor(UserAgentInterceptor())
            .build()
    }

    private operator fun File.div(pathSegment: String) = File(this, pathSegment)

    fun provideCloudMessaging() = CloudMessaging()
}

val Context.component: AppComponent get() = (applicationContext as AppComponentProvider).appComponent

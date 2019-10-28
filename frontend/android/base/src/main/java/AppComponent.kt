package com.nicolasmilliard.socialcats

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.jakewharton.byteunits.BinaryByteUnit.MEBIBYTES
import com.nicolasmilliard.socialcats.auth.Auth
import com.nicolasmilliard.socialcats.auth.AuthImpl
import com.nicolasmilliard.socialcats.auth.ui.AndroidAuthUi
import com.nicolasmilliard.socialcats.base.BuildConfig
import com.nicolasmilliard.socialcats.cloudmessaging.AndroidInstanceIdProvider
import com.nicolasmilliard.socialcats.cloudmessaging.CloudMessaging
import com.nicolasmilliard.socialcats.cloudmessaging.InstanceIdProvider
import com.nicolasmilliard.socialcats.http.UserAgentInterceptor
import com.nicolasmilliard.socialcats.session.SessionManager
import com.nicolasmilliard.socialcats.store.SocialCatsFirestore
import com.nicolasmilliard.socialcats.store.SocialCatsStore
import com.nicolasmilliard.socialcats.ui.AndroidNetworkManager
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY
import okhttp3.logging.HttpLoggingInterceptor.Logger

private val logger = KotlinLogging.logger {}

interface AppComponentProvider {
    val appComponent: AppComponent
}

class AppComponent(val app: Application, scope: CoroutineScope) {
    val httpClient by lazy { AppModule.provideOkHttp(app) }
    val auth by lazy { AppModule.provideAuth() }
    val authUi by lazy { AppModule.provideAuthUi(app, auth) }
    val firestore by lazy { AppModule.provideFirestore() }
    val store by lazy { AppModule.provideSocialCatsStore(firestore) }
    val sessionManager by lazy {
        val manager = AppModule.provideSessionManager(auth, store)
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
        val cloudMessaging = AppModule.provideCloudMessaging(AndroidInstanceIdProvider(), sessionManager, store)
        scope.launch {
            cloudMessaging.start()
        }
        cloudMessaging
    }
}

object AppModule {

    fun provideConnectivityChecker(context: Context): ConnectivityChecker {
        val connectivityManager = context.getSystemService<ConnectivityManager>()
        requireNotNull(connectivityManager)
        return ConnectivityChecker(AndroidNetworkManager(connectivityManager))
    }

    fun provideAuthUi(context: Context, auth: Auth): AndroidAuthUi {
        return AndroidAuthUi(context, AuthUI.getInstance(), auth)
    }

    fun provideAuth(): Auth {
        return AuthImpl(FirebaseAuth.getInstance())
    }

    fun provideFirestore(): FirebaseFirestore {
        return Firebase.firestore
    }

    fun provideSocialCatsStore(firestore: FirebaseFirestore): SocialCatsStore =
        SocialCatsFirestore(firestore)

    fun provideSessionManager(auth: Auth, store: SocialCatsStore) = SessionManager(auth, store)

    fun provideOkHttp(application: Application): OkHttpClient {
        val cacheDir = application.cacheDir / "http"

        return OkHttpClient.Builder()
            .cache(Cache(cacheDir, MEBIBYTES.toBytes(10)))
            .addNetworkInterceptor(
                HttpLoggingInterceptor(object : Logger {
                    override fun log(message: String) = logger.debug { message }
                }).apply { level = if (BuildConfig.DEBUG) BODY else BASIC })
            .addNetworkInterceptor(UserAgentInterceptor())
            .build()
    }

    private operator fun File.div(pathSegment: String) = File(this, pathSegment)

    fun provideCloudMessaging(
        instanceIdProvider: InstanceIdProvider,
        sessionManager: SessionManager,
        store: SocialCatsStore
    ): CloudMessaging {
        return CloudMessaging(instanceIdProvider, sessionManager, store)
    }
}

val Context.component: AppComponent get() = (applicationContext as AppComponentProvider).appComponent

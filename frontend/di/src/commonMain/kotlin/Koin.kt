package com.nicolasmilliard.socialcats.di

import com.nicolasmilliard.socialcats.ConnectivityChecker
import com.nicolasmilliard.socialcats.auth.Auth
import com.nicolasmilliard.socialcats.cloudmessaging.CloudMessaging
import com.nicolasmilliard.socialcats.session.SessionManager
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(platformModule, coreModule)
}

private val coreModule = module {
    single(named("app")) { MainScope() + CoroutineName("App") }
    single {
        val auth = Auth(get())
        val scope: CoroutineScope = get(named("app"))
        scope.launch { auth.start() }
        auth
    }
    single { AppInitializer(get(named("app")), get(), get(), get(), get()) }
    single {
        val manager = SessionManager(get(), get(), get())
        val scope: CoroutineScope = get(named("app"))
        scope.launch { manager.start() }
        manager
    }
    single {
        val checker = ConnectivityChecker(get())
        val scope: CoroutineScope = get(named("app"))
        scope.launch { checker.start() }
        checker
    }

    single {
        val messaging = CloudMessaging()
        val scope: CoroutineScope = get(named("app"))
        scope.launch { messaging.start() }
        messaging
    }
}

expect val platformModule: Module

package com.nicolasmilliard.socialcats.searchapi

import com.nicolasmilliard.socialcats.payment.FakePaymentProcessor
import com.nicolasmilliard.socialcats.payment.PaymentProcessor
import com.nicolasmilliard.socialcats.search.repository.FakeSearchRepository
import com.nicolasmilliard.socialcats.search.repository.SearchRepository
import com.nicolasmilliard.socialcats.store.FakeUserStoreAdmin
import com.nicolasmilliard.socialcats.store.UserStoreAdmin
import io.ktor.config.ApplicationConfig
import org.koin.core.module.Module
import org.koin.dsl.module

class TestAppComponent {
    val userStore = FakeUserStoreAdmin()
    val searchRepository = FakeSearchRepository()
    val paymentProcessor = FakePaymentProcessor()

    fun getTestModules(config: ApplicationConfig): List<Module> {

        val mods = getModules(config).toMutableList()
        mods.add(module(override = true) {
            single<UserStoreAdmin> {
                userStore
            }
        })
        mods.add(module(override = true) {
            single<PaymentProcessor> {
                paymentProcessor
            }
        })
        mods.add(module(override = true) {

            single<SearchRepository> {
                searchRepository
            }
        })
        mods.add(module(override = true) {
            single<FirebaseTokenVerifier> {
                FakeFirebaseAuth()
            }
        })

        return mods
    }
}

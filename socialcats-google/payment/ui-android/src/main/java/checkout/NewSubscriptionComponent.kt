package com.nicolasmilliard.socialcats.payment.ui.checkout

import com.nicolasmilliard.socialcats.payment.AndroidStripeService
import com.nicolasmilliard.socialcats.payment.PaymentLoader
import com.nicolasmilliard.socialcats.payment.PaymentServiceModule
import com.nicolasmilliard.socialcats.payment.StripeService
import com.nicolasmilliard.socialcats.payment.presenter.NewSubscriptionPresenter
import org.koin.dsl.bind
import org.koin.dsl.module

val mainModule = module {

    factory {
        PaymentServiceModule.paymentService(get())
    }

    factory {
        PaymentLoader(get(), get())
    }

    factory {
        NewSubscriptionPresenter(get(), get(), get())
    }

    single {
        val stripe = AndroidStripeService(get(), "pk_test_gyrNQtTH4QNioAr0mMbSLaqb")
        stripe.init()
        stripe
    } bind StripeService::class
}

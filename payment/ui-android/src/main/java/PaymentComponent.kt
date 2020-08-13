package com.nicolasmilliard.socialcats.payment.ui

import androidx.lifecycle.viewModelScope
import com.nicolasmilliard.socialcats.payment.AndroidStripeService
import com.nicolasmilliard.socialcats.payment.PaymentLoader
import com.nicolasmilliard.socialcats.payment.PaymentServiceModule
import com.nicolasmilliard.socialcats.payment.StripeService
import com.nicolasmilliard.socialcats.payment.presenter.ManageSubscriptionPresenter
import com.nicolasmilliard.socialcats.payment.presenter.CheckoutSubscriptionPresenter
import com.nicolasmilliard.socialcats.payment.ui.checkout.NewSubscriptionViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.bind
import org.koin.dsl.module

class PaymentComponent {

    fun load() = loadKoinModules(mainModule)
    fun unLoad() = unloadKoinModules(mainModule)

    private val mainModule = module {

        factory {
            PaymentServiceModule.paymentService(get())
        }

        factory {
            PaymentLoader(get(), get())
        }

        single {
            val stripe = AndroidStripeService(get(), "pk_test_gyrNQtTH4QNioAr0mMbSLaqb")
            stripe.init()
            stripe
        } bind StripeService::class

        factory {
            ManageSubscriptionPresenter(get(), get(), get())
        }

        viewModel {
            val presenter: ManageSubscriptionPresenter = get()
            val viewModel = ManageSubscriptionViewModel(presenter)
            viewModel.viewModelScope.launch {
                presenter.start()
            }
            viewModel
        }

//        factory {
//            NewSubscriptionPresenter(get(), get(), get())
//        }
//
//        viewModel {
//            val presenter: NewSubscriptionPresenter = get()
//            val viewModel = NewSubscriptionViewModel(presenter)
//            viewModel.viewModelScope.launch {
//                presenter.start()
//            }
//            viewModel
//        }
//
        factory {
            CheckoutSubscriptionPresenter(get(), get(), get())
        }

        viewModel {
            val presenter: CheckoutSubscriptionPresenter = get()
            val viewModel = NewSubscriptionViewModel(presenter)
            viewModel.viewModelScope.launch {
                presenter.start()
            }
            viewModel
        }
    }
}

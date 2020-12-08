package com.nicolasmilliard.socialcats.payment.ui.checkout

import androidx.lifecycle.ViewModel
import com.nicolasmilliard.socialcats.payment.presenter.CheckoutSubscriptionPresenter
import timber.log.Timber

class NewSubscriptionViewModel(val presenter: CheckoutSubscriptionPresenter) : ViewModel() {

    override fun onCleared() {
        super.onCleared()
        Timber.i("onCleared")
    }
}

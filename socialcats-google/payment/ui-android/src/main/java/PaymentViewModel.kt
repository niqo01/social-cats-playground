package com.nicolasmilliard.socialcats.payment.ui

import androidx.lifecycle.ViewModel
import timber.log.Timber

class PaymentViewModel(private val component: PaymentComponent = PaymentComponent()) : ViewModel() {

    init {
        component.load()
    }

    override fun onCleared() {
        super.onCleared()
        Timber.i("onCleared")
        component.unLoad()
    }
}

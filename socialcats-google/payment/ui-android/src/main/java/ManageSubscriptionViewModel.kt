package com.nicolasmilliard.socialcats.payment.ui

import androidx.lifecycle.ViewModel
import com.nicolasmilliard.socialcats.payment.presenter.ManageSubscriptionPresenter
import timber.log.Timber

class ManageSubscriptionViewModel(val presenter: ManageSubscriptionPresenter) : ViewModel() {

    override fun onCleared() {
        super.onCleared()
        Timber.i("onCleared")
    }
}

package com.nicolasmilliard.socialcats.account.ui

import androidx.lifecycle.ViewModel
import com.nicolasmilliard.socialcats.account.presenter.AccountPresenter
import timber.log.Timber

class AccountViewModel(val presenter: AccountPresenter) : ViewModel() {

    override fun onCleared() {
        super.onCleared()
        Timber.i("onCleared")
    }
}

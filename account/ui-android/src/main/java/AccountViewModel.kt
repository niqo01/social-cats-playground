package com.nicolasmilliard.socialcats.account.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nicolasmilliard.socialcats.account.presenter.AccountPresenter
import com.nicolasmilliard.socialcats.account.provideAccountPresenter
import com.nicolasmilliard.socialcats.component
import kotlinx.coroutines.launch
import timber.log.Timber

class AccountViewModel(application: Application) : AndroidViewModel(application) {

    val accountPresenter: AccountPresenter by lazy {
        application.component.provideAccountPresenter().apply {
            viewModelScope.launch {
                start()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Timber.i("onCleared")
    }
}

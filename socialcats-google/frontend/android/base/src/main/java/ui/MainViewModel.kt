package com.nicolasmilliard.socialcats.ui

import androidx.lifecycle.ViewModel
import com.nicolasmilliard.socialcats.search.presenter.MainPresenter
import timber.log.Timber

class MainViewModel(val presenter: MainPresenter) : ViewModel() {

    override fun onCleared() {
        super.onCleared()
        Timber.i("onCleared")
    }
}

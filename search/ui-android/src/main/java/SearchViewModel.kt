package com.nicolasmilliard.socialcats.search.ui

import androidx.lifecycle.ViewModel
import com.nicolasmilliard.socialcats.search.presenter.SearchPresenter
import timber.log.Timber

class SearchViewModel(val presenter: SearchPresenter) : ViewModel() {

    override fun onCleared() {
        super.onCleared()
        Timber.i("onCleared")
    }
}

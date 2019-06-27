package com.nicolasmilliard.socialcats.search.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nicolasmilliard.socialcats.component
import com.nicolasmilliard.socialcats.search.presenter.SearchPresenter
import com.nicolasmilliard.socialcats.search.provideSearchPresenter
import kotlinx.coroutines.launch
import timber.log.Timber

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    val searchPresenter: SearchPresenter by lazy {
        application.component.provideSearchPresenter().apply {
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

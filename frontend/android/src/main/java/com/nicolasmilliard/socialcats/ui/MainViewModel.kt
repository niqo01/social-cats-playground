package com.nicolasmilliard.socialcats.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nicolasmilliard.socialcats.component
import com.nicolasmilliard.socialcats.search.presenter.MainPresenter
import com.nicolasmilliard.socialcats.search.provideMainPresenter
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val mainPresenter: MainPresenter by lazy {
        application.component.provideMainPresenter().apply {
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

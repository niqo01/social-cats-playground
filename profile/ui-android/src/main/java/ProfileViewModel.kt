package com.nicolasmilliard.socialcats.profile.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import timber.log.Timber

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    override fun onCleared() {
        super.onCleared()
        Timber.i("onCleared")
    }
}

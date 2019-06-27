package com.nicolasmilliard.socialcats.ui

import android.app.Activity
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.nicolasmilliard.presentation.UiBinder
import com.nicolasmilliard.socialcats.base.R
import com.nicolasmilliard.socialcats.base.databinding.ActivityMainBinding
import com.nicolasmilliard.socialcats.search.presenter.MainPresenter
import timber.log.Timber

class MainUiBinder(
    private val binding: ActivityMainBinding,
    activity: Activity,
    private val events: (MainPresenter.Event) -> Unit
) : UiBinder<MainPresenter.Model> {

    private var snackbar: Snackbar? = null

    init {
        val navController = activity.findNavController(R.id.navHostFragment)
        val bottomNav: BottomNavigationView = binding.bottomNav
        bottomNav.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            bottomNav.isVisible = destination.label != "LoadingFragment"
        }
    }

    override fun bind(model: MainPresenter.Model, oldModel: MainPresenter.Model?) {

        Timber.i("model: $model")

        if (model.authStatus == MainPresenter.Model.SignInStatus.FAILED) {
            var snackbar = this.snackbar
            if (snackbar == null) {
                snackbar = Snackbar.make(binding.root, "Unknown error", BaseTransientBottomBar.LENGTH_INDEFINITE)
                    .setAction(R.string.retry) {
                        events(MainPresenter.Event.RetryAnonymousSignIn)
                    }
                this.snackbar = snackbar
            }
            snackbar.show()
        } else {
            snackbar?.dismiss()
        }
    }
}

package com.nicolasmilliard.socialcats.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.install.model.UpdateAvailability
import com.nicolasmilliard.presentation.bindTo
import com.nicolasmilliard.socialcats.base.databinding.ActivityMainBinding
import com.nicolasmilliard.socialcats.search.presenter.MainPresenter
import com.nicolasmilliard.socialcats.search.presenter.MainPresenter.Event.AnonymousUpdateMergeConflict
import kotlinx.coroutines.launch
import timber.log.Timber

private const val SIGN_IN_REQUEST_CODE = 6666
private const val IN_APP_UPDATE_REQUEST_CODE = 6667

class MainActivity : AppCompatActivity() {

    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var mainPresenter: MainPresenter

    private lateinit var binding: ActivityMainBinding

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("onCreate")

        if ("true" == intent.getStringExtra("crash")) {
            Timber.e("Synthetic crash signal detected. Throwing in 3.. 2.. 1..")
            throw RuntimeException("Crash! Bang! Pow! This is only a test...")
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appUpdateManager = AppUpdateManagerFactory.create(this)

        checkForAppUpdate()

        mainPresenter = mainViewModel.mainPresenter

        lifecycleScope.launch {
            val binder = MainUiBinder(binding, this@MainActivity, mainPresenter.events)
            binder.bindTo(mainPresenter)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            IN_APP_UPDATE_REQUEST_CODE -> {
                if (requestCode != RESULT_OK) {
                    Timber.e("Failed to update app, trying again")
                    checkForAppUpdate()
                }
            }
            SIGN_IN_REQUEST_CODE -> {

                if (requestCode == SIGN_IN_REQUEST_CODE) {
                    val response = IdpResponse.fromResultIntent(data)

                    // Successfully signed in
                    if (resultCode == AppCompatActivity.RESULT_OK) {
                        Timber.i("Auth UI successfull sign in activity result")
                    } else {
                        // Sign in failed
                        if (response == null) {
                            // User pressed back button
                            return
                        }

                        when (response.error!!.errorCode) {
                            ErrorCodes.NO_NETWORK -> Snackbar.make(
                                binding.root,
                                "No internet connection",
                                Snackbar.LENGTH_LONG
                            ).show()
                            ErrorCodes.ANONYMOUS_UPGRADE_MERGE_CONFLICT -> {
                                val nonAnonymousCredential = response.credentialForLinking
                                mainPresenter.events(AnonymousUpdateMergeConflict(nonAnonymousCredential!!))
                            }
                            else -> Snackbar.make(
                                binding.root,
                                "Unknown error",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        appUpdateManager.appUpdateInfo
            .addOnSuccessListener {
                if (it.updateAvailability() ==
                    UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                ) {
                    startUpdateFlow(it)
                }
            }
    }

    private fun checkForAppUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener {
            if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                it.isUpdateTypeAllowed(IMMEDIATE)
            ) {
                Timber.i("Update available, version code: ${it.availableVersionCode()}")
                // TODO We would probably check that the update is necessary here
                // Like setting a min version code in remote config
                startUpdateFlow(it)
            }
        }
    }

    private fun startUpdateFlow(updateInfo: AppUpdateInfo) {
        appUpdateManager.startUpdateFlowForResult(
            // Pass the intent that is returned by 'getAppUpdateInfo()'.
            updateInfo,
            // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
            IMMEDIATE,
            // The current activity making the update request.
            this,
            // Include a request code to later monitor this update request.
            IN_APP_UPDATE_REQUEST_CODE
        )
    }
}

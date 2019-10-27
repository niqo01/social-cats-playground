package com.nicolasmilliard.socialcats.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.install.model.UpdateAvailability
import com.nicolasmilliard.socialcats.R
import com.nicolasmilliard.socialcats.auth.ui.AndroidAuthUi
import com.nicolasmilliard.socialcats.component
import com.nicolasmilliard.socialcats.databinding.ActivityMainBinding
import com.nicolasmilliard.socialcats.session.SessionManager
import com.nicolasmilliard.socialcats.session.SessionState.NoSession
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import mu.KotlinLogging
import timber.log.Timber

private val logger = KotlinLogging.logger {}

private const val SIGN_IN_REQUEST_CODE = 6666
private const val IN_APP_UPDATE_REQUEST_CODE = 6667

class MainActivity : AppCompatActivity() {

    private lateinit var authUi: AndroidAuthUi
    private lateinit var sessionManager: SessionManager
    private lateinit var appUpdateManager: AppUpdateManager

    private lateinit var binding: ActivityMainBinding

    init {
        lifecycleScope.launch {
            whenCreated {
                sessionManager.sessions.collect {
                    when (it) {
                        is NoSession -> {
                            try {
                                authUi.silentSignIn()
                            } catch (e: Throwable) {
                                logger.info(e) { "Silently sign in failed" }
                                Snackbar.make(
                                    binding.root,
                                    "Unknown error",
                                    Snackbar.LENGTH_INDEFINITE
                                ).setAction(R.string.retry) {
                                    launch {
                                        authUi.silentSignIn()
                                    }
                                }.show()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logger.info { "onCreate" }

        if ("true" == intent.getStringExtra("crash")) {
            Timber.e("Synthetic crash signal detected. Throwing in 3.. 2.. 1..")
            throw RuntimeException("Crash! Bang! Pow! This is only a test...")
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appUpdateManager = AppUpdateManagerFactory.create(this)
        authUi = component.authUi
        sessionManager = component.sessionManager

        val navController = findNavController(R.id.navHostFragment)
        val bottomNav: BottomNavigationView = binding.bottomNav
        bottomNav.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            bottomNav.isVisible = destination.label != "LoadingFragment"
        }
        checkForAppUpdate()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            IN_APP_UPDATE_REQUEST_CODE -> {
                if (requestCode != RESULT_OK) {
                    logger.error { "Failed to update app, trying again" }
                    checkForAppUpdate()
                }
            }
            SIGN_IN_REQUEST_CODE -> {

                if (requestCode == SIGN_IN_REQUEST_CODE) {
                    val response = IdpResponse.fromResultIntent(data)

                    // Successfully signed in
                    if (resultCode == AppCompatActivity.RESULT_OK) {
                        logger.info { "Auth UI successfull sign in activity result" }
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
                logger.info { "Update available, version code: ${it.availableVersionCode()}" }
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

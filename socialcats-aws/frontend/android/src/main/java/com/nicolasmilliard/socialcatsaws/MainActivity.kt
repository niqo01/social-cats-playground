package com.nicolasmilliard.socialcatsaws

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.navigate
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.nicolasmilliard.activityresult.ActivityResultFlow
import com.nicolasmilliard.activityresult.Event
import com.nicolasmilliard.socialcatsaws.auth.Auth
import com.nicolasmilliard.socialcatsaws.billing.BillingRepository
import com.nicolasmilliard.socialcatsaws.billing.BillingRepository.LaunchBillingFlowResult
import com.nicolasmilliard.socialcatsaws.home.Home
import com.nicolasmilliard.socialcatsaws.home.HomePresenter
import com.nicolasmilliard.socialcatsaws.imageupload.ImageUploadNav
import com.nicolasmilliard.socialcatsaws.profile.Profile
import com.nicolasmilliard.socialcatsaws.profile.ProfilePresenter
import com.nicolasmilliard.socialcatsaws.ui.SocialCatsAwsTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

  @Inject
  lateinit var auth: Auth

  @Inject
  lateinit var imageUploadNav: ImageUploadNav

  @Inject
  lateinit var activityResultFlow: ActivityResultFlow

  @Inject
  lateinit var billingRepository: BillingRepository

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      SocialCatsAwsTheme {
        nav()
      }
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    Timber.i("MainActivity.onActivityResult() $requestCode, $resultCode")
    Timber.d("MainActivity.onActivityResult() $data")
    lifecycleScope.launch {
      activityResultFlow.produceEvent(Event(requestCode, resultCode, data))
    }
  }

  @Composable
  fun nav() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "home") {
      home(navController)
      profile(navController)
    }
  }

  fun NavGraphBuilder.home(navController: NavHostController) {
    navigation(startDestination = "nestedHome", route = "home") {
      composable("nestedHome") {
        val presenter: HomePresenter = hiltNavGraphViewModel()
        presenter.setLauncher(object : HomePresenter.Launcher {
          override fun signIn(): Boolean {
            val scope = lifecycleScope
            scope.launch {
              auth.signInWithWebUI(this@MainActivity)
            }
            return true
          }

          override fun launchProfile(id: String): Boolean {
            navController.navigate("profile/$id")
            return true
          }
        })
        Home(presenter.models, presenter.events)
      }
    }
  }

  fun NavGraphBuilder.profile(navController: NavHostController) {
    composable(
      "profile/{userId}",
      arguments = listOf(navArgument("userId") { type = NavType.StringType })
    ) { backStackEntry ->
      val presenter: ProfilePresenter = hiltNavGraphViewModel()
      presenter.setLauncher(object : ProfilePresenter.Launcher {
        override fun onNavUp(): Boolean {
          navController.navigateUp()
          return true
        }

        override fun onUpload(): Boolean {
          imageUploadNav.startPickerFlow(this@MainActivity)
          return true
        }

        override suspend fun launchBillingFlow(originalJson: String): LaunchBillingFlowResult {
          return billingRepository.launchBillingFlow(this@MainActivity, originalJson)
        }
      })

      val userId = backStackEntry.arguments!!.getString("userId")!!
      presenter.loadProfile(userId)
      Profile(presenter.models, presenter.events)
    }
  }
}

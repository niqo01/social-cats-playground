package com.nicolasmilliard.socialcatsaws.auth

import android.app.Activity
import android.content.Intent
import com.amplifyframework.auth.AuthCategory
import com.amplifyframework.auth.AuthChannelEventName
import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession
import com.amplifyframework.auth.result.AuthSessionResult
import com.amplifyframework.core.InitializationStatus
import com.amplifyframework.hub.HubCategory
import com.amplifyframework.hub.HubChannel
import com.amplifyframework.hub.SubscriptionToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal class CognitoAuth(
  private val scope: CoroutineScope,
  private val auth: AuthCategory,
  private val hub: HubCategory
) : AuthProvider {

  private val _authStates = MutableStateFlow<AuthState>(AuthState.Unknown)
  override val authStates: StateFlow<AuthState> get() = _authStates

  private var subToken: SubscriptionToken? = null

  init {
    _authStates.subscriptionCount
      .map { count -> count > 0 } // map count into active/inactive flag
      .distinctUntilChanged() // only react to true<->false changes
      .onEach { isActive -> // configure an action
        if (isActive) subscribeToAuthEvent() else unSubscribeToAuthEvent()
      }
      .launchIn(scope)
  }

  private fun subscribeToAuthEvent() {
    subToken = hub.subscribe(HubChannel.AUTH) { event ->
      when (event.name) {
        InitializationStatus.SUCCEEDED.name -> {
          Timber.i("Auth successfully initialized")
        }
        InitializationStatus.FAILED.name -> {
          Timber.i("Auth failed to succeed")
        }
        else -> {
          when (AuthChannelEventName.valueOf(event.name)) {
            AuthChannelEventName.SIGNED_IN -> {
              Timber.i("Auth just became signed in.")
              setAuthState()
            }
            AuthChannelEventName.SIGNED_OUT -> {
              Timber.i("Auth just became signed out.")
              _authStates.value = AuthState.SignOut
            }
            AuthChannelEventName.SESSION_EXPIRED -> {
              Timber.i("Auth session just expired.")
              setAuthState()
            }
          }
        }
      }
    }
    setAuthState()
  }

  private fun unSubscribeToAuthEvent() {
    val token = subToken
    if (token != null) hub.unsubscribe(token)
  }

  private fun setAuthState() {
    scope.launch {
      val session = fetchAuthSession()
      if (session.isSignedIn) {
        val userId = session.identityId.value!!
        val poolTokensResult = session.userPoolTokens
        if (poolTokensResult.type == AuthSessionResult.Type.FAILURE) {
          _authStates.value =
            AuthState.SessionExpired(userId)
        } else {
          _authStates.value =
            AuthState.SignedIn(userId, poolTokensResult.value!!.accessToken)
        }
      } else {
        _authStates.value = AuthState.SignOut
      }
    }
  }

  override suspend fun signInWithWebUI(callingActivity: Activity): Boolean =
    suspendCoroutine { cont ->
      auth.signInWithWebUI(
        callingActivity,
        {
          cont.resume(it.isSignInComplete)
        },
        {
          if (it is AuthException.UserCancelledException) {
            cont.resume(false)
          } else {
            cont.resumeWithException(it)
          }
        }
      )
    }

  override suspend fun signOut(): Unit = suspendCoroutine { cont ->
    auth.signOut({ cont.resume(Unit) }, { cont.resumeWithException(it) })
  }

  suspend fun fetchAuthSession(): AWSCognitoAuthSession = suspendCoroutine { cont ->
    auth.fetchAuthSession(
      {
        cont.resume(it as AWSCognitoAuthSession)
      },
      {
        cont.resumeWithException(it)
      }
    )
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == AWSCognitoAuthPlugin.WEB_UI_SIGN_IN_ACTIVITY_CODE) {
      auth.handleWebUISignInResponse(data)
    }
  }
}

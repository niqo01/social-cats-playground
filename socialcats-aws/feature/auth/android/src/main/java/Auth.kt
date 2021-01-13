package com.nicolasmilliard.socialcatsaws.auth

import android.app.Activity
import android.content.Intent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

public class Auth(private val provider: AuthProvider) {

  public val authStates: Flow<AuthState> = provider.authStates

  public fun getAccessToken(userId: String): String? {
    val state = provider.authStates.value
    if (state is AuthState.SignedIn && state.userId == userId) {
      return state.accessToken
    }
    return null
  }

  public suspend fun signInWithWebUI(activity: Activity) {
    provider.signInWithWebUI(activity)
  }

  public suspend fun signOut(): Unit = provider.signOut()

  public fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    provider.onActivityResult(requestCode, resultCode, data)
  }
}

public sealed class AuthState {
  public object Unknown : AuthState()
  public data class SignedIn(
    val userId: String,
    val accessToken: String
  ) : AuthState()

  public object SignOut : AuthState()
  public data class SessionExpired(val userId: String) : AuthState()
}

public interface AuthProvider {

  public val authStates: StateFlow<AuthState>

  public suspend fun signInWithWebUI(callingActivity: Activity): Boolean
  public suspend fun signOut()
  public fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
}

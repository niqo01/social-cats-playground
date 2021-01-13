package com.nicolasmilliard.socialcatsaws.auth

import android.content.Context
import androidx.startup.Initializer
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify

public class CognitoInitializer : Initializer<Unit> {
  override fun create(context: Context) {
    Amplify.addPlugin(AWSCognitoAuthPlugin())
  }
  override fun dependencies(): List<Class<out Initializer<*>>> {
    return emptyList()
  }
}

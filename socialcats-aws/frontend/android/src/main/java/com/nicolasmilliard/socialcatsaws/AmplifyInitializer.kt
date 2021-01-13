package com.nicolasmilliard.socialcatsaws

import android.content.Context
import androidx.startup.Initializer
import com.amplifyframework.core.Amplify
import com.nicolasmilliard.socialcatsaws.auth.CognitoInitializer

class AmplifyInitializer : Initializer<Unit> {
  override fun create(context: Context) {
    Amplify.configure(context)
  }

  override fun dependencies(): List<Class<out Initializer<*>>> {
    return listOf(CognitoInitializer::class.java)
  }
}

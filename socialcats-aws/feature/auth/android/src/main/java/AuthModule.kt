package com.nicolasmilliard.socialcatsaws.auth

import com.amplifyframework.core.Amplify
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
public object AuthModule {

  @Singleton
  @Provides
  public fun provideAuth(scope: CoroutineScope): Auth {
    val cognitoAuth = CognitoAuth(scope, Amplify.Auth, Amplify.Hub)
    return Auth(cognitoAuth)
  }
}

package com.nicolasmilliard.socialcatsaws.billing

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
public object BillingModule {

  @Singleton
  @Provides
  public fun provideBillingRepository(@ApplicationContext context: Context, scope: CoroutineScope): BillingRepository {
    return BillingRepository(context, BillingWebservice(), scope)
  }
}

package com.nicolasmilliard.socialcatsaws.profile

import com.nicolasmilliard.cloudmetric.CloudMetrics
import com.nicolasmilliard.socialcatsaws.profile.repository.UsersRepositoryModule

public object ProfileUseCaseModule {

  public fun provideNewUserUseCase(tableName: String, cloudMetrics: CloudMetrics, region: String): NewUserUseCase {
    val repo = UsersRepositoryModule.provideSocialCatsRepository(tableName, cloudMetrics, region)
    return NewUserUseCase(repo)
  }

  public fun provideUpdateUserUseCase(tableName: String, cloudMetrics: CloudMetrics, region: String): UpdateUserUseCase {
    val repo = UsersRepositoryModule.provideSocialCatsRepository(tableName, cloudMetrics, region)
    return UpdateUserUseCase(repo)
  }
}

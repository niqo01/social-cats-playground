package com.nicolasmilliard.socialcatsaws.profile

import com.nicolasmilliard.cloudmetric.CloudMetrics
import com.nicolasmilliard.repository.imageobjectstore.ImageObjectStoreModule
import com.nicolasmilliard.socialcatsaws.profile.repository.UsersRepositoryModule

public object ImageUseCaseModule {

  public fun provideUploadImageUseCase(
    tableName: String,
    bucketName: String,
    cloudMetrics: CloudMetrics,
    region: String
  ): UploadImageUseCase {
    val repo = UsersRepositoryModule.provideSocialCatsRepository(tableName, cloudMetrics, region)
    val imageObjectStore = ImageObjectStoreModule.provideUploadImageObjectStore(bucketName, region)
    return UploadImageUseCase(repo, imageObjectStore, cloudMetrics)
  }
}

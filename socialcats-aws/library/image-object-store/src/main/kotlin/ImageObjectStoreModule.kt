package com.nicolasmilliard.repository.imageobjectstore

import com.nicolasmilliard.socialcatsaws.backend.repository.objectstore.ObjectStoreModule

public object ImageObjectStoreModule {

  public fun provideUploadImageObjectStore(bucketName: String, region: String): ImageObjectStore {
    val objectStore = ObjectStoreModule.provideObjectStore(bucketName, region)
    return ImageObjectStore(objectStore)
  }
}

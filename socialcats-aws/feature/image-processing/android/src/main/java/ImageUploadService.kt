package com.nicolasmilliard.socialcatsaws.imageupload

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.nicolasmilliard.socialcatsaws.api.bearer
import com.nicolasmilliard.socialcatsaws.auth.Auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.RequestBody.Companion.asRequestBody
import timber.log.Timber
import java.io.File

public class ImageUploadService internal constructor(
  private val workManger: WorkManager,
  private val apiImage: ImageUploadApi,
  private val json: Json,
  auth: Auth,
  mainScope: CoroutineScope
) {
  init {
    mainScope.launch {
      auth.signOutEvents
        .collect {
          Timber.i("Canceling Image worked if any.")
          workManger.cancelAllWorkByTag(GetUploadUrlWorker.TAG)
          workManger.cancelAllWorkByTag(ImageUploadWorker.TAG)
        }
    }
  }

  public fun scheduleUpload(userId: String, imagePath: String, optimizeCost: Boolean, optimizeBattery: Boolean) {

    Timber.i("ImageUploadService.scheduleUpload()")
    Timber.d("ImageUploadService.scheduleUpload(), $imagePath")
    val networkType = if (optimizeCost) NetworkType.UNMETERED else NetworkType.CONNECTED
    val uploadUrlConstraints = Constraints.Builder()
      .setRequiredNetworkType(networkType)
      .setRequiresCharging(optimizeBattery)
      .build()
    val getUploadUrlWorkRequest =
      OneTimeWorkRequestBuilder<GetUploadUrlWorker>()
        .setInputData(
          workDataOf(
            GetUploadUrlWorker.INPUT_USER_ID to userId,
            GetUploadUrlWorker.INPUT_IMAGE_PATH to imagePath
          )
        )
        .setConstraints(uploadUrlConstraints)
        .addTag(GetUploadUrlWorker.TAG)
        .build()

    val uploadImageConstraints = Constraints.Builder()
      .setRequiredNetworkType(networkType)
      .setRequiresCharging(optimizeBattery)
      .build()
    val uploadImageWorkRequest =
      OneTimeWorkRequestBuilder<ImageUploadWorker>()
        .setConstraints(uploadImageConstraints)
        .addTag(ImageUploadWorker.TAG)
        .build()
    workManger.beginUniqueWork(imagePath, ExistingWorkPolicy.KEEP, getUploadUrlWorkRequest)
      .then(uploadImageWorkRequest)
      .enqueue()
  }

  public suspend fun getUploadUrl(token: String): GetUploadUrl {
    Timber.i("ImageUploadService.getUploadUrl()")
    val preSignRequest = apiImage.getUploadUrl(bearer(token)).preSignRequest
    if (preSignRequest is MaxStoredImagesReached) {
      return GetUploadUrl.MaxStoredImagesReached
    }
    return GetUploadUrl.Success(json.encodeToString(preSignRequest as UploadData))
  }

  public suspend fun doUpload(uploadData: String, imagePath: String) {
    Timber.i("ImageUploadService.doUpload()")
    val data: UploadData = json.decodeFromString(uploadData)
    val requestBody = File(imagePath).asRequestBody()
    apiImage.uploadImage(data.url, data.headers, requestBody)
  }
}

public sealed class GetUploadUrl {
  public data class Success(val uploadData: String) : GetUploadUrl()
  public object MaxStoredImagesReached : GetUploadUrl()
}

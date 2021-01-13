package com.nicolasmilliard.socialcatsaws.imageupload

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import retrofit2.HttpException
import timber.log.Timber
import java.net.SocketTimeoutException

@HiltWorker
public class ImageUploadWorker @AssistedInject constructor(
  @Assisted context: Context,
  @Assisted params: WorkerParameters,
  private val uploadImageService: ImageUploadService
) : CoroutineWorker(context, params) {

  override suspend fun doWork(): Result {
    Timber.i("ImageUploadWorker.doWork()")
    val imagePath =
      inputData.getString(INPUT_IMAGE_PATH)
    val uploadData =
      inputData.getString(INPUT_UPLOAD_DATA)

    if (imagePath == null) {
      Timber.e("imagePath is required")
      return Result.failure()
    }
    if (uploadData == null) {
      Timber.e("uploadData is required")
      return Result.failure()
    }

    return try {
      uploadImageService.doUpload(uploadData, imagePath)
      Result.success()
    } catch (e: SocketTimeoutException) {
      Timber.w(e, "Socket Timeout while trying to upload profile.image")
      Result.retry()
    } catch (e: HttpException) {
      if (e.code() in 500..599) {
        Timber.w(e, "Backend error")
        Result.retry()
      } else {
        Timber.e(e, "Failure while trying to upload profile.image")
        Result.failure()
      }
    }
  }

  public companion object {
    public const val INPUT_IMAGE_PATH: String = "IMAGE_PATH"
    public const val INPUT_UPLOAD_DATA: String = "INPUT_UPLOAD_DATA"
    public const val TAG: String = "uploadImage"
  }
}

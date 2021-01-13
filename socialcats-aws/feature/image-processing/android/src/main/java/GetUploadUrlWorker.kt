package com.nicolasmilliard.socialcatsaws.imageupload

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.nicolasmilliard.socialcatsaws.auth.Auth
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import retrofit2.HttpException
import timber.log.Timber
import java.net.SocketTimeoutException

@HiltWorker
public class GetUploadUrlWorker @AssistedInject constructor(
  @Assisted context: Context,
  @Assisted params: WorkerParameters,
  private val uploadImageService: ImageUploadService,
  private val auth: Auth
) : CoroutineWorker(context, params) {

  override suspend fun doWork(): Result {
    Timber.i("ImageUploadWorker.doWork()")
    val imagePath =
      inputData.getString(INPUT_IMAGE_PATH)
    val userId =
      inputData.getString(INPUT_USER_ID)
    if (userId == null ||
      imagePath == null
    ) {
      Timber.e("Null inputData or token")
      return Result.failure()
    }
    return try {
      val token = auth.getAccessToken(userId)
      if (token == null) {
        Timber.w("ImageUploadWorker no token available")
        return Result.failure()
      }
      when (val uploadUrl = uploadImageService.getUploadUrl(token)) {
        is GetUploadUrl.MaxStoredImagesReached -> {
          Timber.i("ImageUploadWorker Max stored images reached")
          Result.failure(workDataOf("cause" to FAILURE_MAX_STORED_IMAGES_REACHED))
        }
        is GetUploadUrl.Success -> Result.success(
          workDataOf(
            ImageUploadWorker.INPUT_IMAGE_PATH to imagePath,
            ImageUploadWorker.INPUT_UPLOAD_DATA to uploadUrl.uploadData,
          )
        )
      }
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
    public const val INPUT_USER_ID: String = "USER_ID"
    public const val INPUT_IMAGE_PATH: String = "IMAGE_PATH"
    public const val FAILURE_MAX_STORED_IMAGES_REACHED: String = "MAX_STORED_IMAGES_REACHED"
    public const val TAG: String = "uploadImage"
  }
}

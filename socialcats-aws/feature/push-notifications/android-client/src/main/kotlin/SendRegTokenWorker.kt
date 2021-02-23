package com.nicolasmilliard.pushnotification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
public class SendRegTokenWorker @AssistedInject constructor(
  @Assisted context: Context,
  @Assisted params: WorkerParameters,
  private val regTokenService: RegTokenService
) : CoroutineWorker(context, params) {

  override suspend fun doWork(): Result {
    Timber.i("SendRegTokenWorker.doWork()")
    val userId =
      inputData.getString(INPUT_USER_ID)
    if (userId == null
    ) {
      Timber.e(IllegalArgumentException("SendRegTokenWorker Null userId"))
      return Result.failure()
    }
    return when (val result = regTokenService.syncToken(userId)) {
      is RegTokenService.SyncTokenResult.Success -> Result.success()
      is RegTokenService.SyncTokenResult.Failure -> if (result.canBeRetried) Result.retry() else Result.failure()
    }
  }

  public companion object {
    public const val INPUT_USER_ID: String = "INPUT_USER_ID"
    public const val TAG: String = "uploadToken"
  }
}

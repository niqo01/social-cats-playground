package com.nicolasmilliard.socialcats.store

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import timber.log.Timber

class SyncStoreWorker(private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val store = appContext.store
        try {
            store.waitForPendingWrites()
            Timber.i("Sync worker Store done")
        } catch (e: Throwable) {
            Timber.e(e, "Error while waiting for pending writes")
            return Result.retry()
        }
        return Result.success()
    }
}

fun WorkManager.requestStoreSync() {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
    val request = OneTimeWorkRequestBuilder<SyncStoreWorker>()
        .setConstraints(constraints)
        .build()
    enqueueUniqueWork("syncStore", ExistingWorkPolicy.REPLACE, request)
}

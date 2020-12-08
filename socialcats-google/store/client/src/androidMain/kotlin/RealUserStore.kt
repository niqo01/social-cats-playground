package com.nicolasmilliard.socialcats.store

import androidx.work.WorkManager
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreException.Code.NOT_FOUND
import com.google.firebase.firestore.FirebaseFirestoreException.Code.UNAVAILABLE
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.nicolasmilliard.socialcats.store.DbConstants.Collections.InstanceIds
import com.nicolasmilliard.socialcats.store.DbConstants.Collections.Users
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.tasks.await

class RealUserStore(private val db: FirebaseFirestore, private val workManager: WorkManager) : UserStore {

    override suspend fun waitForPendingWrites() {
        db.waitForPendingWrites().await()
    }

    override suspend fun saveDeviceInfo(userId: String, deviceInfo: DeviceInfo) {
        val data = mapOf(
            InstanceIds.Fields.TOKEN to deviceInfo.token,
            InstanceIds.Fields.LANGUAGE_TAG to deviceInfo.languageTag
        )
        db.collection(Users.NAME)
            .document(userId)
            .collection(InstanceIds.name)
            .document(deviceInfo.instanceId)
            .set(data, SetOptions.merge())
            .await()
        workManager.requestStoreSync()
    }

    override suspend fun deviceInfo(userId: String, instanceId: String, cacheOnly: Boolean): DeviceInfo? =
        notFoundToNull {
            db.collection(Users.NAME)
                .document(userId)
                .collection(InstanceIds.name)
                .document(instanceId)
                .get(if (cacheOnly) Source.CACHE else Source.DEFAULT)
                .await()
                .toDeviceInfo()
        }

    override suspend fun user(uid: String, cacheOnly: Boolean): User? =
        notFoundToNull {
            val snapshot = db.collection(Users.NAME)
                .document(uid)
                .get(if (cacheOnly) Source.CACHE else Source.DEFAULT)
                .await()
            return@notFoundToNull if (snapshot.exists()) snapshot.toUser() else null
        }

    override suspend fun user(uid: String): Flow<User> =
        callbackFlow {
            val listener = db
                .collection(Users.NAME)
                .document(uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        close(e)
                    } else {
                        if (snapshot == null) {
                            close(IllegalStateException("Snapshot is null or does not exist: $snapshot"))
                        } else {
                            if (snapshot.exists()) {
                                offer(snapshot.toUser())
                            } // does not exist when doc is empty with existing subcollection
                        }
                    }
                }
            awaitClose { listener.remove() }
        }.conflate()
}

fun DocumentSnapshot.toUser(): User {
    val name = getString(Users.Fields.NAME)
    val createdAt = checkNotNull(getTimestamp(Users.Fields.CREATED_AT))
    val photoUrl = getString(Users.Fields.PHOTO_URL)
    val isMember = getBoolean(Users.Fields.MEMBERSHIP_STATUS) ?: false
    return User(id, name, createdAt.toDate().time, photoUrl, isMember)
}

fun DocumentSnapshot.toDeviceInfo(): DeviceInfo {
    val token = checkNotNull(getString(InstanceIds.Fields.TOKEN))
    val languageTag = checkNotNull(getString(InstanceIds.Fields.LANGUAGE_TAG))
    return DeviceInfo(id, token, languageTag)
}

suspend inline fun <T> notFoundToNull(noinline block: suspend () -> T): T? {
    return try {
        block()
    } catch (e: FirebaseFirestoreException) {
        if (e.code == NOT_FOUND ||
            e.code == UNAVAILABLE
        ) { // Firebase support Case 00031299
            null
        } else {
            throw e
        }
    }
}

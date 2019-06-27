package com.nicolasmilliard.socialcats.store

import androidx.work.WorkManager
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.nicolasmilliard.socialcats.store.DbConstants.Collections.InstanceIds
import com.nicolasmilliard.socialcats.store.DbConstants.Collections.Users
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SocialCatsFirestore(private val db: FirebaseFirestore, private val workManager: WorkManager) : SocialCatsStore {

    override suspend fun waitForPendingWrites() {
        db.waitForPendingWrites().await()
    }

    override suspend fun saveDeviceInfo(userId: String, deviceInfo: DeviceInfo): Unit = withContext(Dispatchers.IO) {
        val data = mapOf(
            InstanceIds.Fields.TOKEN to deviceInfo.token,
            InstanceIds.Fields.LANGUAGE_TAG to deviceInfo.languageTag
        )
        db.collection(Users.NAME)
            .document(userId)
            .collection(InstanceIds.name)
            .document(deviceInfo.instanceId)
            .set(data, SetOptions.merge())
        workManager.requestStoreSync()
    }

    override suspend fun getDeviceInfo(userId: String, instanceId: String, cacheOnly: Boolean): DeviceInfo? {
        return db
            .collection(Users.NAME)
            .document(userId)
            .collection(InstanceIds.name)
            .document(instanceId)
            .get(if (cacheOnly) Source.CACHE else Source.DEFAULT)
            .await()
            .toDeviceInfo()
    }

    override suspend fun getCurrentUser(uid: String, cacheOnly: Boolean): User? {
        return db.collection(Users.NAME)
            .document(uid)
            .get(if (cacheOnly) Source.CACHE else Source.DEFAULT)
            .await()
            .toUser()
    }

    override suspend fun getCurrentUser(uid: String): Flow<User> = withContext(Dispatchers.IO) {
        callbackFlow {
            val listener = db
                .collection(Users.NAME)
                .document(uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        close(e)
                    } else {
                        if (snapshot != null && snapshot.exists()) {
                            offer(snapshot.toUser()!!)
                        }
                    }
                }
            awaitClose { listener.remove() }
        }.conflate()
    }
}

fun DocumentSnapshot.toUser(): User? {
    if (!exists()) return null
    val name = getString(Users.Fields.NAME)
    val createdAt = getTimestamp(Users.Fields.CREATED_AT)!!
    val photoUrl = getString(Users.Fields.PHOTO_URL)
    return User(id, name, createdAt.toDate().time, photoUrl)
}

fun DocumentSnapshot.toDeviceInfo(): DeviceInfo? {
    if (!exists()) return null
    val token = getString(InstanceIds.Fields.TOKEN)!!
    val languageTag = getString(InstanceIds.Fields.LANGUAGE_TAG)!!
    return DeviceInfo(id, token, languageTag)
}

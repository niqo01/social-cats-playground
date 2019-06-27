package com.nicolasmilliard.socialcats.store

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import com.nicolasmilliard.socialcats.store.DbConstants.COLLECTIONS_INSTANCE_IDS
import com.nicolasmilliard.socialcats.store.DbConstants.COLLECTIONS_USERS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SocialCatsFirestore(private val db: FirebaseFirestore) : SocialCatsStore {

    override suspend fun saveInstanceId(userId: String, deviceInfo: DeviceInfo): Unit = withContext(Dispatchers.IO) {
        db.collection(COLLECTIONS_USERS)
            .document(userId)
            .collection(COLLECTIONS_INSTANCE_IDS)
            .document(deviceInfo.instanceId)
            .set(StoreInstanceId(deviceInfo), SetOptions.merge()).await()
    }

    override suspend fun getCurrentUser(uid: String): Flow<User> = withContext(Dispatchers.IO) {
        callbackFlow {
            val listener = db
                .collection(COLLECTIONS_USERS)
                .document(uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        close(e)
                    } else {
                        if (snapshot != null && snapshot.exists()) {
                            val fUser = snapshot.toObject<StoreUser>()
                            if (fUser != null) {
                                offer(User(uid, fUser.name, fUser.createdAt!!.toDate().time, null))
                            }
                        }
                    }
                }
            awaitClose { listener.remove() }
        }.conflate()
    }
}

private data class StoreUser(
    val name: String? = null,
    val createdAt: Timestamp? = null
)

private data class StoreInstanceId(
    val token: String? = null,
    val languageTag: String? = null
) {
    constructor(deviceInfo: DeviceInfo) : this(deviceInfo.token, deviceInfo.languageTag)
}

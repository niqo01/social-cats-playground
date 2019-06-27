package com.nicolasmilliard.socialcats.store

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

class SocialCatsFirestore(val db: FirebaseFirestore) : SocialCatsStore {
    override fun getCurrentUser(uid: String): Flow<User> {
        return callbackFlow {
            val listener = db
                .collection("users")
                .document(uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        close(e)
                    } else {
                        if (snapshot != null && snapshot.exists()) {
                            val fUser = snapshot.toObject<FirestoreUser>()
                            if (fUser != null) {
                                offer(User(uid, fUser.name, fUser.createdAt.toDate().time, null))
                            }
                        }
                    }
                }
            awaitClose { listener.remove() }
        }.conflate()
    }
}

private data class FirestoreUser(
    val name: String? = null,
    val createdAt: Timestamp = Timestamp.now()
)

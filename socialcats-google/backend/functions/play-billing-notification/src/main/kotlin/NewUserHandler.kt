package com.nicolasmilliard.socialcats.auth

import com.nicolasmilliard.socialcats.store.InsertUser
import com.nicolasmilliard.socialcats.store.UserStoreAdmin

class NewUserHandler(private val store: UserStoreAdmin) {

    suspend operator fun invoke(authUser: UserRecord) {

        val isAnonymous = authUser.providerData == null
        // We don't save anonymous users
        if (isAnonymous) return

        val userInfo = authUser.providerData!![0]

        val insertUser =
            InsertUser(
                authUser.uid,
                authUser.displayName ?: userInfo.displayName,
                authUser.phoneNumber ?: userInfo.phoneNumber,
                authUser.email ?: userInfo.email,
                authUser.emailVerified,
                authUser.photoURL ?: userInfo.photoURL
            )
        store.createUser(insertUser)
    }
}

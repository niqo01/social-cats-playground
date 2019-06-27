package com.nicolasmilliard.socialcats.session

import com.nicolasmilliard.socialcats.auth.anAuthToken
import com.nicolasmilliard.socialcats.store.aStoreUser

val anInstanceIdProvider = object : InstanceIdProvider {
    override suspend fun getId() = "fakeId"

    override val languageTag: String
    get() = "fr"
}

val aUserDevice = UserDevice("fakeId", null, "fr")

val unAuthNoDeviceSession = Session(null, null)
val unAuthSession = Session(null, aUserDevice)
val authSession = Session(AuthData(anAuthToken.token, false, aStoreUser), aUserDevice)
val authSessionNoDevice = Session(AuthData(anAuthToken.token, false, aStoreUser), null)
val authSessionAnonymous = Session(AuthData(anAuthToken.token, true, aStoreUser), aUserDevice)
val authSessionNoUserNoDevice = Session(AuthData(anAuthToken.token, false, null), null)
val authSessionNoUser = Session(AuthData(anAuthToken.token, false, null), aUserDevice)

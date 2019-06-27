package com.nicolasmilliard.socialcats.session

import com.nicolasmilliard.socialcats.auth.anAuthToken
import com.nicolasmilliard.socialcats.auth.anAuthUser
import com.nicolasmilliard.socialcats.store.DeviceInfo
import com.nicolasmilliard.socialcats.store.aDeviceInfo
import com.nicolasmilliard.socialcats.store.aStoreUser

class FakeDeviceInfoProvider(var deviceInfo: DeviceInfo) : DeviceInfoProvider {
    override suspend fun getDeviceInfo(): DeviceInfo {
        return deviceInfo
    }
}

val unAuthNoDeviceSession = Session(null, null)
val unAuthSession = Session(null, aDeviceInfo)
val authSession = Session(AuthData(anAuthUser.uid, anAuthToken.token, false, aStoreUser), aDeviceInfo)
val authSessionNoDevice = Session(AuthData(anAuthUser.uid, anAuthToken.token, false, aStoreUser), null)
val authSessionAnonymous = Session(AuthData(anAuthUser.uid, anAuthToken.token, true, aStoreUser), aDeviceInfo)
val authSessionNoUserNoDevice = Session(AuthData(anAuthUser.uid, anAuthToken.token, false, null), null)
val authSessionNoUser = Session(AuthData(anAuthUser.uid, anAuthToken.token, false, null), aDeviceInfo)

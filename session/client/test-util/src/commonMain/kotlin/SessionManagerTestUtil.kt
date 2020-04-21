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

val unknownAuthSession = Session(SessionAuthState.Unknown, aDeviceInfo)
val unknownAuthSessionNoDevice = Session(SessionAuthState.Unknown, null)
val unAuthNoDeviceSession = Session(SessionAuthState.UnAuthenticated, null)
val unAuthSession = Session(SessionAuthState.UnAuthenticated, aDeviceInfo)
val authSession = Session(SessionAuthState.Authenticated.User(anAuthUser.uid, anAuthToken.token, aStoreUser), aDeviceInfo)
val authSessionNoTokenNoUser = Session(SessionAuthState.Authenticated.User(anAuthUser.uid, null, null), aDeviceInfo)
val authSessionNoToken = Session(SessionAuthState.Authenticated.User(anAuthUser.uid, null, aStoreUser), aDeviceInfo)
val authSessionNoDevice = Session(SessionAuthState.Authenticated.User(anAuthUser.uid, anAuthToken.token, aStoreUser), null)
val authSessionAnonymous = Session(SessionAuthState.Authenticated.Anonymous(anAuthUser.uid, anAuthToken.token), aDeviceInfo)
val authSessionNoUserNoDevice = Session(SessionAuthState.Authenticated.User(anAuthUser.uid, anAuthToken.token, null), null)
val authSessionNoUser = Session(SessionAuthState.Authenticated.User(anAuthUser.uid, anAuthToken.token, null), aDeviceInfo)

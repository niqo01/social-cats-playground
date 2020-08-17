package com.nicolasmilliard.socialcats.session

import com.google.firebase.iid.FirebaseInstanceId
import com.nicolasmilliard.socialcats.store.DeviceInfo
import kotlinx.coroutines.tasks.await
import java.util.Locale

class AndroidInstanceIdProvider : DeviceInfoProvider {

    override suspend fun getDeviceInfo(): DeviceInfo {
        val languageTag = Locale.getDefault().toLanguageTag()
        val instanceId = FirebaseInstanceId.getInstance().instanceId.await()
        return DeviceInfo(instanceId.id, instanceId.token, languageTag)
    }
}

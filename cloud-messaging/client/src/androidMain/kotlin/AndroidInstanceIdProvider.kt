package com.nicolasmilliard.socialcats.cloudmessaging

import com.google.firebase.iid.FirebaseInstanceId
import java.util.Locale
import kotlinx.coroutines.tasks.await

class AndroidInstanceIdProvider : InstanceIdProvider {
    override val languageTag: String
        get() = Locale.getDefault().toLanguageTag()

    override suspend fun getId() = FirebaseInstanceId.getInstance().instanceId.await().id
}

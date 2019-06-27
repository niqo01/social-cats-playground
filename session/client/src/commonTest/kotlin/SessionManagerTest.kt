package com.nicolasmilliard.socialcats.session

import com.nicolasmilliard.socialcats.auth.Auth
import com.nicolasmilliard.socialcats.auth.FakeAuthProvider
import com.nicolasmilliard.socialcats.auth.anAuthUser
import com.nicolasmilliard.socialcats.auth.anNewAuthToken
import com.nicolasmilliard.socialcats.store.FakeStore
import com.nicolasmilliard.socialcats.store.aDeviceInfo
import com.nicolasmilliard.socialcats.store.aStoreUser
import com.nicolasmilliard.socialcats.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class SessionManagerTest {

    private lateinit var fakeAuthProvider: FakeAuthProvider
    private lateinit var auth: Auth
    private lateinit var fakeStore: FakeStore
    private lateinit var fakeDeviceInfoProvider: FakeDeviceInfoProvider

    @BeforeTest
    fun before() {
        fakeAuthProvider = FakeAuthProvider()
        fakeStore = FakeStore()
        auth = Auth(fakeAuthProvider)
        fakeDeviceInfoProvider = FakeDeviceInfoProvider(aDeviceInfo)
    }

    @Test
    fun `Test UnAuthenticated session`() = runTest {
        withTimeout(500) {
            fakeAuthProvider.offerUser(null)

            var sessionManager = SessionManager(auth, fakeStore, fakeDeviceInfoProvider)
            val job = startComponents(this, sessionManager)

            sessionManager.sessions.filter { it == unAuthSession }.first()
            job.cancel()
        }
    }

    @Test
    fun `Test Authenticated session`() = runTest {
        withTimeout(500) {
            fakeStore.offer(aStoreUser)
            fakeAuthProvider.offerUser(anAuthUser)
            fakeAuthProvider.offerToken(anNewAuthToken)

            var sessionManager = SessionManager(auth, fakeStore, fakeDeviceInfoProvider)
            val job = startComponents(this, sessionManager)

            sessionManager.sessions.filter { it == authSession }.first()
            job.cancel()
        }
    }

    @Test
    fun `Test Saving token`() = runTest {
        withTimeout(500000) {
            fakeAuthProvider.offerUser(null)
            fakeAuthProvider.offerToken(null)

            var sessionManager = SessionManager(auth, fakeStore, fakeDeviceInfoProvider)
            fakeDeviceInfoProvider.deviceInfo = fakeDeviceInfoProvider.deviceInfo.copy(token = "token2")
            sessionManager.onNewDeviceIdToken("token2")

            val job = startComponents(this, sessionManager)

            fakeAuthProvider.offerUser(anAuthUser)
            fakeAuthProvider.offerToken(anNewAuthToken)
            fakeStore.offer(aStoreUser)

            sessionManager.sessions.onEach { logger.info { "Received $it" } }.filter { it.device?.token == "token2" }.first()

            job.cancel()
        }
    }

    private fun startComponents(scope: CoroutineScope, presenter: SessionManager): Job {
        val job = Job()
        scope.apply {
            launch(Dispatchers.Default + job) {
                auth.start()
            }
            launch(Dispatchers.Default + job) {
                presenter.start()
            }
        }
        return job
    }
}

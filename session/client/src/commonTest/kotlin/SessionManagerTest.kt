package com.nicolasmilliard.socialcats.session

import com.nicolasmilliard.socialcats.auth.Auth
import com.nicolasmilliard.socialcats.auth.FakeAuthProvider
import com.nicolasmilliard.socialcats.auth.anAuthToken
import com.nicolasmilliard.socialcats.auth.anAuthUser
import com.nicolasmilliard.socialcats.store.FakeStore
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
    private lateinit var instanceIdProvider: InstanceIdProvider

    @BeforeTest
    fun before() {
        fakeAuthProvider = FakeAuthProvider()
        fakeStore = FakeStore()
        auth = Auth(fakeAuthProvider)
        instanceIdProvider = anInstanceIdProvider
    }

    @Test
    fun `Test UnAuthenticated session`() = runTest {
        withTimeout(500) {
            fakeAuthProvider.offerUser(null)

            var sessionManager = SessionManager(auth, fakeStore, instanceIdProvider)
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
            fakeAuthProvider.offerToken(anAuthToken)

            var sessionManager = SessionManager(auth, fakeStore, instanceIdProvider)
            val job = startComponents(this, sessionManager)

            sessionManager.sessions.filter { it == authSession }.first()
            job.cancel()
        }
    }

    @Test
    fun `Test Saving token`() = runTest {
        withTimeout(500) {
            fakeAuthProvider.offerUser(null)
            fakeAuthProvider.offerToken(null)

            var sessionManager = SessionManager(auth, fakeStore, instanceIdProvider)
            val job = startComponents(this, sessionManager)

            sessionManager.onNewToken("token2")

            fakeAuthProvider.offerUser(anAuthUser)
            fakeAuthProvider.offerToken(anAuthToken)
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

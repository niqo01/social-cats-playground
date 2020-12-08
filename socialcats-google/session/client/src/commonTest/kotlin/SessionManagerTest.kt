package com.nicolasmilliard.socialcats.session

import app.cash.turbine.test
import com.nicolasmilliard.socialcats.auth.Auth
import com.nicolasmilliard.socialcats.auth.FakeAuthProvider
import com.nicolasmilliard.socialcats.auth.anAuthUser
import com.nicolasmilliard.socialcats.auth.anNewAuthToken
import com.nicolasmilliard.socialcats.store.FakeStore
import com.nicolasmilliard.socialcats.store.aDeviceInfo
import com.nicolasmilliard.socialcats.store.aStoreUser
import com.nicolasmilliard.socialcats.test.runTest
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import mu.KotlinLogging
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private val logger = KotlinLogging.logger {}

class SessionManagerTest {

    private lateinit var fakeAuthProvider: FakeAuthProvider
    private lateinit var auth: Auth
    private lateinit var fakeStore: FakeStore
    private lateinit var fakeDeviceInfoProvider: FakeDeviceInfoProvider
    private lateinit var appScope: CoroutineScope

    @BeforeTest
    fun before() {
        fakeAuthProvider = FakeAuthProvider()
        fakeStore = FakeStore()
        auth = Auth(fakeAuthProvider)
        fakeDeviceInfoProvider = FakeDeviceInfoProvider(aDeviceInfo)
        appScope = CoroutineScope(Dispatchers.Default) + CoroutineName("App")
    }

    @AfterTest
    fun after() {
        appScope.cancel()
    }

    @Test
    fun `Test UnAuthenticated session`() = runTest {
        logger.info { "Test UnAuthenticated session" }
        var sessionManager = SessionManager(auth, fakeStore, fakeDeviceInfoProvider)
        startComponents(appScope, sessionManager)

        val authorizedStates =
            mutableSetOf(unknownAuthSessionNoDevice, unknownAuthSession, unAuthNoDeviceSession, unAuthSession)

        fakeAuthProvider.offerUser(0, null)

        sessionManager.sessions.test(500) {
            do {
                val item = expectItem()
                assertTrue(authorizedStates.remove(item), "Item not expected: $item")
            } while (item != unAuthSession)
            delay(100)
            expectNoEvents()
            cancel()
        }
    }

    @Test
    fun `Test Authenticated session`() = runTest {
        logger.info { "Test Authenticated session" }
        var sessionManager = SessionManager(auth, fakeStore, fakeDeviceInfoProvider)
        startComponents(appScope, sessionManager)

        val authorizedStates = mutableSetOf(
            unknownAuthSessionNoDevice,
            unknownAuthSession,
            unAuthNoDeviceSession,
            authSessionNoTokenNoUser,
            authSessionNoToken,
            authSessionNoUser,
            authSession
        )

        fakeStore.offer(aStoreUser)
        fakeAuthProvider.offerUser(0, anAuthUser)
        fakeAuthProvider.offerToken(anNewAuthToken)

        sessionManager.sessions.test(500) {
            do {
                val item = expectItem()
                assertTrue(authorizedStates.remove(item), "Item not expected: $item")
            } while (item != authSession)
            delay(100)
            expectNoEvents()
            cancel()
        }
    }

    @Test
    fun `Test Saving token`() = runTest {
        logger.info { "Test Saving token" }
        fakeAuthProvider.offerUser(0, null)
        fakeAuthProvider.offerToken(null)

        var sessionManager = SessionManager(auth, fakeStore, fakeDeviceInfoProvider)
        fakeDeviceInfoProvider.deviceInfo = fakeDeviceInfoProvider.deviceInfo.copy(token = "token2")

        startComponents(appScope, sessionManager)

        fakeAuthProvider.offerUser(0, anAuthUser)
        fakeAuthProvider.offerToken(anNewAuthToken)
        fakeStore.offer(aStoreUser)

        sessionManager.sessions.test(500) {
            do {
                val item = expectItem()
            } while (!item.isAuthenticated && (item.device != null))
            delay(100)
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals("token2", fakeStore.savedDeviceInfo!!.token)
    }

    private fun startComponents(scope: CoroutineScope, presenter: SessionManager) {
        scope.apply {
            launch {
                auth.start()
            }
            launch {
                presenter.start()
            }
        }
    }
}

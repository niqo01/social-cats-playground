package com.nicolasmilliard.socialcats.session

import com.nicolasmilliard.socialcats.auth.AuthState.Authenticated
import com.nicolasmilliard.socialcats.auth.AuthState.UnAuthenticated
import com.nicolasmilliard.socialcats.auth.FakeAuth
import com.nicolasmilliard.socialcats.auth.aAuthUser
import com.nicolasmilliard.socialcats.auth.aToken
import com.nicolasmilliard.socialcats.session.SessionState.NoSession
import com.nicolasmilliard.socialcats.session.SessionState.Session
import com.nicolasmilliard.socialcats.store.FakeStore
import com.nicolasmilliard.socialcats.store.aStoreUser
import com.nicolasmilliard.socialcats.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class SessionManagerTest {

    private lateinit var fakeAuth: FakeAuth
    private lateinit var fakeStore: FakeStore

    @BeforeTest
    fun before() {
        fakeAuth = FakeAuth()
        fakeStore = FakeStore()
    }

    @Test
    fun noSession() = runTest {
        withTimeout(2000) {
            fakeStore.offer(aStoreUser)
            fakeAuth.offer(UnAuthenticated)

            var sessionManager = SessionManager(fakeAuth, fakeStore)
            val job = launch(Dispatchers.Default) {
                sessionManager.start()
            }

            val actual = sessionManager.sessions.first()
            assertEquals(NoSession, actual)
            job.cancel()
        }
    }

    @Test
    fun session() = runTest {
        withTimeout(2000) {
            coroutineScope {
                fakeStore.offer(aStoreUser)
                fakeAuth.offer(
                    Authenticated(
                        aToken,
                        aAuthUser
                    )
                )

                var sessionManager = SessionManager(fakeAuth, fakeStore)

                val job = launch(Dispatchers.Default) {
                    sessionManager.start()
                }

                val actual = sessionManager.sessions.first()

                val expected = Session("token", aStoreUser)
                assertEquals(expected, actual)
                job.cancel()
            }
        }
    }
}

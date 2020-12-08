package com.nicolasmilliard.socialcats.search.presenter

import com.nicolasmilliard.socialcats.ConnectivityChecker
import com.nicolasmilliard.socialcats.FakeNetworkManager
import com.nicolasmilliard.socialcats.auth.Auth
import com.nicolasmilliard.socialcats.auth.AuthToken
import com.nicolasmilliard.socialcats.auth.FakeAuthProvider
import com.nicolasmilliard.socialcats.auth.NewToken
import com.nicolasmilliard.socialcats.auth.anAuthUser
import com.nicolasmilliard.socialcats.search.FakeSearchService
import com.nicolasmilliard.socialcats.search.SearchLoader
import com.nicolasmilliard.socialcats.search.SearchService
import com.nicolasmilliard.socialcats.search.aUser
import com.nicolasmilliard.socialcats.search.presenter.SearchPresenter.Model
import com.nicolasmilliard.socialcats.search.presenter.SearchPresenter.Model.LoadingState.Success
import com.nicolasmilliard.socialcats.search.presenter.SearchPresenter.Model.QueryResults
import com.nicolasmilliard.socialcats.session.FakeDeviceInfoProvider
import com.nicolasmilliard.socialcats.session.SessionManager
import com.nicolasmilliard.socialcats.store.FakeStore
import com.nicolasmilliard.socialcats.store.aDeviceInfo
import com.nicolasmilliard.socialcats.store.aStoreUser
import com.nicolasmilliard.socialcats.test.runTest
import com.nicolasmilliard.socialcats.test.test
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
import kotlin.test.assertTrue

private val logger = KotlinLogging.logger {}

class SearchPresenterTest {

    private lateinit var fakeAuthProvider: FakeAuthProvider
    private lateinit var fakeDeviceInfoProvider: FakeDeviceInfoProvider
    private lateinit var auth: Auth
    private lateinit var fakeStore: FakeStore
    private lateinit var sessionManager: SessionManager
    private lateinit var searchService: SearchService
    private lateinit var searchLoader: SearchLoader
    private lateinit var networkManager: FakeNetworkManager
    private lateinit var connectivityChecker: ConnectivityChecker
    private lateinit var appScope: CoroutineScope

    private val unAuthState = Model(isConnected = true, loadingState = Model.LoadingState.UnAuthenticated)
    private val loadingState = Model(isConnected = true, loadingState = Model.LoadingState.Loading)
    private val successState = Model(
        isConnected = true,
        loadingState = Success(
            1,
            QueryResults(
                "",
                listOf(aUser)
            ),
            refreshing = Model.RefreshState.IDLE
        )
    )

    @BeforeTest
    fun before() {
        fakeAuthProvider = FakeAuthProvider()
        fakeDeviceInfoProvider = FakeDeviceInfoProvider(aDeviceInfo)
        fakeStore = FakeStore()
        auth = Auth(fakeAuthProvider)
        sessionManager = SessionManager(auth, fakeStore, fakeDeviceInfoProvider)
        searchService = FakeSearchService()
        searchLoader = SearchLoader(searchService)
        networkManager = FakeNetworkManager()
        connectivityChecker = ConnectivityChecker(networkManager)
        appScope = CoroutineScope(Dispatchers.Default) + CoroutineName("App")
    }

    @AfterTest
    fun after() {
        appScope.cancel()
    }

    @Test
    fun testDefaultStateAndNotAuthenticated() = runTest {

        val searchPresenter =
            SearchPresenter(sessionManager, searchLoader, connectivityChecker)
        startComponents(appScope, searchPresenter)

        val authorizedStates =
            mutableSetOf(loadingState, unAuthState)

        fakeStore.offer(aStoreUser)
        fakeAuthProvider.offerUser(0, null)
        networkManager.offer(true)

        searchPresenter.models.test(500) {
            do {
                val item = expectItem()
                assertTrue(authorizedStates.remove(item), "Item not expected: $item")
            } while (item != unAuthState)
            delay(100)
            expectNoEvents()
            cancel()
        }
    }

    @Test
    fun testDefaultStateAndAuthenticated() = runTest {

        val searchPresenter =
            SearchPresenter(sessionManager, searchLoader, connectivityChecker)
        startComponents(appScope, searchPresenter)

        val authorizedStates =
            mutableSetOf(unAuthState, loadingState, successState)

        fakeStore.offer(aStoreUser)
        fakeAuthProvider.offerUser(0, anAuthUser)
        fakeAuthProvider.offerToken(NewToken(AuthToken("token"), anAuthUser))
        networkManager.offer(true)

        searchPresenter.models.test {
            do {
                val item = expectItem()
                assertTrue(authorizedStates.remove(item), "Item not expected: $item")
            } while (item != successState)
            delay(100)
            expectNoEvents()
            cancel()
        }
    }

    private fun startComponents(scope: CoroutineScope, presenter: SearchPresenter) {
        scope.apply {
            launch {
                auth.start()
            }
            launch {
                sessionManager.start()
            }
            launch {
                connectivityChecker.start()
            }
            launch {
                presenter.start()
            }
        }
    }
}

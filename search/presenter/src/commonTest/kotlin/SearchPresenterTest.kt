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

    private val unAuthState = Model(isConnected = true, loadingState = Model.LoadingState.UnAuthenticated)
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
    }

    @Test
    fun `Test default state and not authenticated`() = runTest {
        withTimeout(500) {

            fakeStore.offer(aStoreUser)
            fakeAuthProvider.offerUser(null)
            networkManager.offer(true)

            val searchPresenter =
                SearchPresenter(sessionManager, searchLoader, connectivityChecker)

            val job = startComponents(this, searchPresenter)

            searchPresenter.models.onEach { logger.info { "Received $it" } }.filter { it == unAuthState }.first()

            job.cancel()
        }
    }

    @Test
    fun `Test default state and authenticated`() = runTest {
        withTimeout(500) {

            fakeStore.offer(aStoreUser)
            fakeAuthProvider.offerUser(anAuthUser)
            fakeAuthProvider.offerToken(NewToken(AuthToken("token"), anAuthUser))
            networkManager.offer(true)

            val searchPresenter =
                SearchPresenter(sessionManager, searchLoader, connectivityChecker)

            val job = startComponents(this, searchPresenter)

            searchPresenter.models.onEach { logger.info { "Received $it" } }.filter { it == successState }.first()
            logger.info { "Before cancel" }
            job.cancel()
        }
    }

    private fun startComponents(scope: CoroutineScope, presenter: SearchPresenter): Job {
        val job = Job()
        scope.apply {
            launch(Dispatchers.Default + job) {
                auth.start()
            }
            launch(Dispatchers.Default + job) {
                sessionManager.start()
            }
            launch(Dispatchers.Default + job) {
                connectivityChecker.start()
            }
            launch(Dispatchers.Default + job) {
                presenter.start()
            }
        }
        return job
    }
}

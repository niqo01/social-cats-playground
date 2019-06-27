package com.nicolasmilliard.socialcats.search.presenter

import com.nicolasmilliard.socialcats.ConnectivityChecker
import com.nicolasmilliard.socialcats.FakeNetworkManager
import com.nicolasmilliard.socialcats.auth.AuthState.UnAuthenticated
import com.nicolasmilliard.socialcats.auth.FakeAuth
import com.nicolasmilliard.socialcats.search.FakeSearchService
import com.nicolasmilliard.socialcats.search.SearchLoader
import com.nicolasmilliard.socialcats.search.SearchService
import com.nicolasmilliard.socialcats.search.aUser
import com.nicolasmilliard.socialcats.search.presenter.SearchPresenter.Model
import com.nicolasmilliard.socialcats.search.presenter.SearchPresenter.Model.LoadingState.Loading
import com.nicolasmilliard.socialcats.search.presenter.SearchPresenter.Model.LoadingState.Success
import com.nicolasmilliard.socialcats.search.presenter.SearchPresenter.Model.QueryResults
import com.nicolasmilliard.socialcats.session.SessionManager
import com.nicolasmilliard.socialcats.store.FakeStore
import com.nicolasmilliard.socialcats.store.aStoreUser
import com.nicolasmilliard.socialcats.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class SearchPresenterTest {

    private lateinit var fakeAuth: FakeAuth
    private lateinit var fakeStore: FakeStore
    private lateinit var sessionManager: SessionManager
    private lateinit var searchService: SearchService
    private lateinit var searchLoader: SearchLoader
    private lateinit var networkManager: FakeNetworkManager
    private lateinit var connectivityChecker: ConnectivityChecker

    private val loading = Model(isConnected = true, loadingState = Loading)
    private val success = Model(
        isConnected = true,
        loadingState = Success(
            1,
            QueryResults(
                "",
                listOf(aUser)
            )
        )
    )

    @BeforeTest
    fun before() {
        fakeAuth = FakeAuth()
        fakeStore = FakeStore()
        sessionManager = SessionManager(fakeAuth, fakeStore)
        searchService = FakeSearchService()
        searchLoader = SearchLoader(searchService)
        networkManager = FakeNetworkManager()
        connectivityChecker = ConnectivityChecker(networkManager)
    }

    @Test
    fun defaultState() = runTest {
        withTimeout(2000) {
            coroutineScope {

                fakeStore.offer(aStoreUser)
                fakeAuth.offer(UnAuthenticated)
                networkManager.offer(true)

                val sessionJob = launch(Dispatchers.Default) {
                    sessionManager.start()
                }

                val searchPresenter =
                    SearchPresenter(sessionManager, searchLoader, connectivityChecker)

                val expected = listOf(loading, success)

                val viewJob = launch {
                    val actual = searchPresenter.models.take(2).toList()
                    assertEquals(expected, actual)
                }

                val presenterJob = launch(Dispatchers.Default) {
                    searchPresenter.start()
                }

                viewJob.join()
                presenterJob.cancel()
                sessionJob.cancel()
            }
        }
    }

//    @Test
//    fun authenticatedState() = runTest {
//        withTimeout(2000) {
//            coroutineScope {
//
//                storeChannel.offer(User("id", "Name", 1L, "photo"))
//                authChannel.offer(Authenticated())
//                networkChannel.offer(true)
//
//                val sessionJob = launch(Dispatchers.Default) {
//                    sessionManager.start()
//                }
//
//                val searchPresenter =
//                    SearchPresenter(sessionManager, searchLoader, connectivityChecker)
//
//                val expected = listOf(loading, success)
//
//                val viewJob = launch {
//                    val actual = searchPresenter.models.take(2).toList()
//                    assertEquals(expected, actual)
//                }
//
//                val presenterJob = launch(Dispatchers.Default) {
//                    searchPresenter.start()
//                }
//
//                viewJob.join()
//                presenterJob.cancel()
//                sessionJob.cancel()
//            }
//        }
//    }
}

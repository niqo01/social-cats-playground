package com.nicolasmilliard.socialcats.search.presenter

import com.nicolasmilliard.socialcats.ConnectivityChecker
import com.nicolasmilliard.socialcats.search.SearchLoader
import com.nicolasmilliard.socialcats.search.SocialCatsApiModule
import com.nicolasmilliard.socialcats.session.SessionManager
import okhttp3.OkHttpClient

object SearchPresenterModule {
    fun provideSearchPresenter(
        sessionManager: SessionManager,
        httpClient: OkHttpClient,
        connectivityChecker: ConnectivityChecker
    ): SearchPresenter {

        val service = SocialCatsApiModule.searchService(httpClient)

        val searchLoader = SearchLoader(service)

        return SearchPresenter(sessionManager, searchLoader, connectivityChecker)
    }
}

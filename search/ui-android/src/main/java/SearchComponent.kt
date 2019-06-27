package com.nicolasmilliard.socialcats.search

import com.nicolasmilliard.socialcats.AppComponent
import com.nicolasmilliard.socialcats.search.presenter.SearchPresenterModule

fun AppComponent.provideSearchPresenter() =
    SearchPresenterModule.provideSearchPresenter(sessionManager, httpClient, connectivityChecker)

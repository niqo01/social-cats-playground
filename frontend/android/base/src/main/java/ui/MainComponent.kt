package com.nicolasmilliard.socialcats.search

import com.nicolasmilliard.socialcats.AppComponent
import com.nicolasmilliard.socialcats.search.presenter.MainPresenter

fun AppComponent.provideMainPresenter() =
    MainPresenter(authUi, auth)

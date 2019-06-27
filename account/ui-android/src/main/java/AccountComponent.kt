package com.nicolasmilliard.socialcats.account

import com.nicolasmilliard.socialcats.AppComponent
import com.nicolasmilliard.socialcats.account.presenter.AccountPresenterModule

fun AppComponent.provideAccountPresenter() = AccountPresenterModule.provideAccountPresenter(authUi, sessionManager)

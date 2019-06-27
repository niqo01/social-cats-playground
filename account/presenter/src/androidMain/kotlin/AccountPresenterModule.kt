package com.nicolasmilliard.socialcats.account.presenter

import com.nicolasmilliard.socialcats.auth.ui.AuthUi
import com.nicolasmilliard.socialcats.session.SessionManager

object AccountPresenterModule {
    fun provideAccountPresenter(
        authUi: AuthUi,
        sessionManager: SessionManager
    ): AccountPresenter {
        return AccountPresenter(authUi, sessionManager)
    }
}

package com.nicolasmilliard.socialcats.account

import androidx.lifecycle.viewModelScope
import com.nicolasmilliard.socialcats.account.presenter.AccountPresenter
import com.nicolasmilliard.socialcats.account.presenter.AccountPresenterModule
import com.nicolasmilliard.socialcats.account.ui.AccountViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

object AccountComponent {
    fun init() = loadKoinModules(mainModule)
}

val mainModule = module {
    factory {
        AccountPresenterModule.provideAccountPresenter(get(), get())
    }
    viewModel {
        val presenter: AccountPresenter = get()
        val viewModel = AccountViewModel(presenter)
        viewModel.viewModelScope.launch {
            presenter.start()
        }
        viewModel
    }
}

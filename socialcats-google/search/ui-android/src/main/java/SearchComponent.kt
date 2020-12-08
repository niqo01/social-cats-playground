package com.nicolasmilliard.socialcats.search

import androidx.lifecycle.viewModelScope
import com.nicolasmilliard.socialcats.search.presenter.SearchPresenter
import com.nicolasmilliard.socialcats.search.presenter.SearchPresenterModule
import com.nicolasmilliard.socialcats.search.ui.SearchViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

object SearchComponent {
    fun init() = loadKoinModules(mainModule)
}

val mainModule = module {

    factory {
        SearchPresenterModule.provideSearchPresenter(get(), get(), get())
    }
    viewModel {
        val presenter: SearchPresenter = get()
        val viewModel = SearchViewModel(presenter)
        viewModel.viewModelScope.launch {
            presenter.start()
        }
        viewModel
    }
}

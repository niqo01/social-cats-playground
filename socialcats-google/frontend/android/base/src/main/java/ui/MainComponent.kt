package com.nicolasmilliard.socialcats.search

import android.content.Context
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.util.CoilUtils
import com.firebase.ui.auth.AuthUI
import com.nicolasmilliard.socialcats.auth.ui.AndroidAuthUi
import com.nicolasmilliard.socialcats.auth.ui.AuthUi
import com.nicolasmilliard.socialcats.search.presenter.MainPresenter
import com.nicolasmilliard.socialcats.ui.MainViewModel
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.bind
import org.koin.dsl.module

object MainComponent {
    fun init() = loadKoinModules(listOf(uiModule, mainModule))
}
val mainModule = module {

    factory {
        MainPresenter(get(), get())
    }
    viewModel {
        val presenter: MainPresenter = get()
        val viewModel = MainViewModel(presenter)
        viewModel.viewModelScope.launch {
            presenter.start()
        }
        viewModel
    }
}

val uiModule = module {
    single {
        AndroidAuthUi(get(), AuthUI.getInstance())
    } bind AuthUi::class

    single {
        val context: Context = get()
        val client: Lazy<OkHttpClient> = get()
        ImageLoader.Builder(context)
            .okHttpClient {
                client.value.newBuilder()
                    .cache(CoilUtils.createDefaultCache(context))
                    .build()
            }.build()
    }
}

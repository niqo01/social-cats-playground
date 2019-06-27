package com.nicolasmilliard.presentation

import kotlinx.coroutines.flow.collect

interface UiBinder<ModelT : Any> {
    fun bind(model: ModelT, oldModel: ModelT?)
}

suspend fun <ModelT : Any> UiBinder<ModelT>.bindTo(presenter: Presenter<ModelT, *>) {
    var oldModel: ModelT? = null
    presenter.models
        .collect {
            bind(it, oldModel)
            oldModel = it
        }
}

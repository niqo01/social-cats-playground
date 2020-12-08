package com.nicolasmilliard.presentation

import kotlinx.coroutines.flow.Flow

interface Presenter<ModelT : Any, EventT : Any> {
    val models: Flow<ModelT>
    val events: (EventT) -> Unit
    suspend fun start()
}

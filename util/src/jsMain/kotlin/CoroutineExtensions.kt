package com.nicolasmilliard.socialcats.test

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual fun Dispatchers.IO(): CoroutineDispatcher = Default

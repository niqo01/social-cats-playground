package com.nicolasmilliard.socialcats.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual fun Dispatchers.IO(): CoroutineDispatcher = Default

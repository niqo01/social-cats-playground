package com.nicolasmilliard.socialcats.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

expect fun Dispatchers.IO(): CoroutineDispatcher

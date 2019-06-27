package com.nicolasmilliard.socialcats.test

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

expect fun Dispatchers.IO(): CoroutineDispatcher

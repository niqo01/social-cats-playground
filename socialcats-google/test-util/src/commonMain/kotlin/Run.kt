package com.nicolasmilliard.socialcats.test

import kotlinx.coroutines.CoroutineScope

expect fun runTest(body: suspend CoroutineScope.() -> Unit)

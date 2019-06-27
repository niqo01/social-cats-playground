package com.nicolasmilliard.socialcats.test

import kotlinx.coroutines.runBlocking

actual fun <T> runTest(block: suspend () -> T) {
    runBlocking { block() }
}

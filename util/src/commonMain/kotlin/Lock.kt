package com.nicolasmilliard.socialcats.util

expect class Lock() {
    fun lock()
    fun unlock()
}

inline fun <R> Lock.withLock(block: () -> R): R {
    try {
        lock()
        return block()
    } finally {
        unlock()
    }
}

package com.nicolasmilliard.socialcats.util

import kotlin.reflect.KClass

/**
 * Does not support isInstance due to Kotlin reflect dependency
 */
fun <T : Throwable> Throwable.isCausedBy(aClass: KClass<T>): Boolean {
    if (this::class == aClass) return true
    return if (cause == null) false else cause!!.isCausedBy(aClass)
}

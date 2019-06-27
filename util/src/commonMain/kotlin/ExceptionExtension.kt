package com.nicolasmilliard.socialcats.util

import kotlin.reflect.KClass

fun <T : Throwable> Throwable.isCausedBy(aClass: KClass<T>): Boolean {
    if (aClass::class.isInstance(this)) return true
    return if (cause == null) false else cause!!.isCausedBy(aClass)
}

package com.nicolasmilliard.socialcats.util

import kotlin.reflect.KClass

fun <T : Throwable> Throwable.isCausedBy(aClass: KClass<T>): Boolean {
    return if (aClass.isInstance(this)) {
        true
    } else {
        if (cause == null) false else cause!!.isCausedBy(aClass)
    }
}

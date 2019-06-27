package com.nicolasmilliard.socialcats.ui.util

import android.view.KeyEvent
import android.view.View

inline fun View.onKey(crossinline body: (KeyEvent) -> Boolean) {
    setOnKeyListener { _, _, event -> body(event) }
}

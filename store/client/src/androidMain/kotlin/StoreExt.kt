package com.nicolasmilliard.socialcats.store

import android.content.Context

interface StoreProvider {
    val store: SocialCatsStore
}

val Context.store get() = (applicationContext as StoreProvider).store

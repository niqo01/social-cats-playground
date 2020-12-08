package com.nicolasmilliard.socialcats.account.ui

import android.content.Context
import android.content.Intent

class ShareHandler(private val context: Context) {
    operator fun invoke() {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "This is my text to send.")
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        context.startActivity(shareIntent)
    }
}

package com.nicolasmilliard.socialcats.account.ui

import android.content.Context
import android.content.Intent
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity

class OssHandler(private val context: Context) {
    operator fun invoke() {
        context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
    }
}

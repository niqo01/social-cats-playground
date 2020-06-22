package com.nicolasmilliard.socialcats.payment

import java.net.URLEncoder

actual fun urlEncode(value: String): String = URLEncoder.encode(value, "utf-8")


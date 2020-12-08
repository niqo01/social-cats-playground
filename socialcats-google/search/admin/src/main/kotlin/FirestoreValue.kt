package com.nicolasmilliard.socialcats.search

import java.util.Date

data class FirestoreValue(
    val resourceId: String,
    val createTime: Date,
    val updateTime: Date,
    val fields: Map<String, Any>
)

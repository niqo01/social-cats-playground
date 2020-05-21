package com.nicolasmilliard.socialcats.auth.ui

import com.nicolasmilliard.socialcats.auth.DeleteStatus

interface AuthUi {
    suspend fun signOut()
    suspend fun silentSignIn()
    suspend fun delete(): DeleteStatus
}

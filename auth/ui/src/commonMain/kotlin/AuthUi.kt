package com.nicolasmilliard.socialcats.auth.ui

interface AuthUi {
    suspend fun signOut()
    suspend fun silentSignIn()
    suspend fun delete()
}

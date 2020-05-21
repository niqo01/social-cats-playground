package com.nicolasmilliard.socialcats.store

interface UserStoreAdmin {
    suspend fun createUser(user: InsertUser)
    suspend fun getCustomerId(uId: String): String?
    suspend fun setPaymentInfo(uId: String, customerUId: String)
    suspend fun setMembershipStatus(uId: String, active: Boolean)
}

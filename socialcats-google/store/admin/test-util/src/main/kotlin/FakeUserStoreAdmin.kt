package com.nicolasmilliard.socialcats.store

class FakeUserStoreAdmin : UserStoreAdmin {
    val users = mutableMapOf<String, String?>()
    val insertedUsers = mutableMapOf<String, InsertUser>()
    val membershipStatusChanged = mutableMapOf<String, Boolean>()

    override suspend fun createUser(user: InsertUser) {
        insertedUsers[user.uid] = user
        users[user.uid] = null
    }

    override suspend fun getCustomerId(uId: String): String? = users[uId]

    override suspend fun setPaymentInfo(uId: String, customerUId: String) {
        users[uId] = customerUId
    }

    override suspend fun setMembershipStatus(uId: String, active: Boolean) {
        membershipStatusChanged[uId] = active
    }
}

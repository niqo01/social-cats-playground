package com.nicolasmilliard.socialcats.search.ui

import androidx.navigation.NavController
import com.nicolasmilliard.socialcats.model.User

class OpenProfileUserHandler(private val navigation: NavController) : UserHandler {
    override fun invoke(user: User) {
        val action = SearchFragmentDirections.actionSearchFragmentToProfileFragment()
        navigation.navigate(action)
    }
}

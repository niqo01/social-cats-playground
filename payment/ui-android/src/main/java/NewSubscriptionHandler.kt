package com.nicolasmilliard.socialcats.payment.ui.new

import androidx.navigation.NavController
import com.nicolasmilliard.socialcats.payment.ui.ManageSubscriptionFragmentDirections

class NewSubscriptionHandler(private val navController: NavController) {
    operator fun invoke() {
        val action =
            ManageSubscriptionFragmentDirections.actionManageSubscriptionFragmentToNewSubscriptionFragment()
        navController.navigate(action)
    }
}

package com.nicolasmilliard.socialcats.payment.ui.checkout

import androidx.fragment.app.Fragment
import com.nicolasmilliard.socialcats.payment.AndroidStripeService

class ConfirmPaymentHandler(private val stripeManager: AndroidStripeService, private val fragment: Fragment) {
    operator fun invoke(paymentMethodId: String, clientSecret: String) {
        stripeManager.confirmCardPayment(fragment, paymentMethodId, clientSecret)
    }
}

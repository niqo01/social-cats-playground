package com.nicolasmilliard.socialcats.payment.ui.checkout

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsIntent.Builder
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navGraphViewModels
import com.nicolasmilliard.presentation.bindTo
import com.nicolasmilliard.socialcats.payment.AndroidStripeService
import com.nicolasmilliard.socialcats.payment.presenter.CheckoutSubscriptionPresenter
import com.nicolasmilliard.socialcats.payment.ui.PaymentViewModel
import com.nicolasmilliard.socialcats.payment.ui.databinding.CheckoutBinding
import com.nicolasmilliard.socialcats.ui.CHECK_CONNECTIVITY_SETTINGS_CODE
import com.nicolasmilliard.socialcats.ui.CheckConnectivityHandler
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class NewSubscriptionFragment : Fragment() {

    private val paymentModel: PaymentViewModel
    by navGraphViewModels(com.nicolasmilliard.socialcats.base.R.id.payment_nav_graph)

    private fun injectFeature() = paymentModel

    private val newSubscriptionViewModel: NewSubscriptionViewModel by viewModel()
    private val stripeService: AndroidStripeService by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        injectFeature()

        val presenter = newSubscriptionViewModel.presenter
        presenter.launcher = Launcher(requireContext())

        val onCheckConnectivityClick = CheckConnectivityHandler(requireActivity(), CHECK_CONNECTIVITY_SETTINGS_CODE)
        val onRequirePaymentConfirmation = ConfirmPaymentHandler(stripeService, this)

        val binding = CheckoutBinding.inflate(inflater, container, false)
        viewLifecycleOwner.lifecycleScope.launch {
            val binder =
                NewSubscriptionUiBinder(binding, presenter.events, onCheckConnectivityClick, onRequirePaymentConfirmation)
            binder.bindTo(presenter)
        }
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Handle the result of stripe.confirmSetupIntent
        if (requestCode == CHECK_CONNECTIVITY_SETTINGS_CODE) {
            Timber.i("Check connectivity result: $resultCode")
        }
    }

    class Launcher(private val context: Context) : CheckoutSubscriptionPresenter.Launcher {
        override fun startCheckout(url: String): Boolean {
            val builder = Builder()
            val customTabsIntent: CustomTabsIntent = builder.build()
            customTabsIntent.launchUrl(context, Uri.parse(url))
            return true
        }
    }
}

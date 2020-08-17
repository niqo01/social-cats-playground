package com.nicolasmilliard.socialcats.payment.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.nicolasmilliard.presentation.bindTo
import com.nicolasmilliard.socialcats.payment.ui.databinding.ManageSubscriptionBinding
import com.nicolasmilliard.socialcats.payment.ui.new.NewSubscriptionHandler
import com.nicolasmilliard.socialcats.ui.CHECK_CONNECTIVITY_SETTINGS_CODE
import com.nicolasmilliard.socialcats.ui.CheckConnectivityHandler
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ManageSubscriptionFragment : Fragment() {

    private val paymentModel: PaymentViewModel
    by navGraphViewModels(com.nicolasmilliard.socialcats.base.R.id.payment_nav_graph)
    private fun injectFeature() = paymentModel

    private val viewModel: ManageSubscriptionViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        injectFeature()

        val presenter = viewModel.presenter

        val onCheckConnectivityClick = CheckConnectivityHandler(requireActivity(), CHECK_CONNECTIVITY_SETTINGS_CODE)
        val onNewSubscriptionClick = NewSubscriptionHandler(findNavController())

        val binding = ManageSubscriptionBinding.inflate(inflater, container, false)
        viewLifecycleOwner.lifecycleScope.launch {
            val binder = ManageSubscriptionUiBinder(binding, presenter.events, onCheckConnectivityClick, onNewSubscriptionClick)
            binder.bindTo(presenter)
        }
        return binding.root
    }
}

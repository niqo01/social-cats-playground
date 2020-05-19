package com.nicolasmilliard.socialcats.account.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.nicolasmilliard.presentation.bindTo
import com.nicolasmilliard.socialcats.account.AccountComponent
import com.nicolasmilliard.socialcats.account.ui.databinding.AccountBinding
import com.nicolasmilliard.socialcats.auth.ui.AndroidAuthUi
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

internal val loadFeature by lazy { AccountComponent.init() }
internal fun injectFeature() = loadFeature

class AccountFragment : Fragment() {

    private val accountViewModel: AccountViewModel by viewModel()
    private val authUi: AndroidAuthUi by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        injectFeature()
        val activity = requireActivity()
        val presenter = accountViewModel.presenter

        val onSignIn = SignInHandler(activity, authUi)
        val onReAuth = ReAuthHandler(activity, authUi)
        val onShare = ShareHandler(activity)
        val onOss = OssHandler(activity)

        val binding = AccountBinding.inflate(inflater, container, false)
        viewLifecycleOwner.lifecycleScope.launch {
            val binder = AccountUiBinder(binding, onSignIn, onReAuth, onShare, onOss, presenter.events)

            binder.bindTo(presenter)
        }

        return binding.root
    }
}

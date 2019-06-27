package com.nicolasmilliard.socialcats.account.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.nicolasmilliard.presentation.bindTo
import com.nicolasmilliard.socialcats.account.ui.databinding.AccountBinding
import com.nicolasmilliard.socialcats.component
import kotlinx.coroutines.launch

class AccountFragment : Fragment() {

    private val accountViewModel: AccountViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val activity = requireActivity()
        val authUi = activity.component.authUi
        val presenter = accountViewModel.accountPresenter

        val onSignIn = SignInHandler(activity, authUi)
        val onShare = ShareHandler(activity)
        val onOss = OssHandler(activity)

        val binding = AccountBinding.inflate(inflater, container, false)
        viewLifecycleOwner.lifecycleScope.launch {
            val binder = AccountUiBinder(binding, onSignIn, onShare, onOss, presenter.events)

            binder.bindTo(presenter)
        }

        return binding.root
    }
}

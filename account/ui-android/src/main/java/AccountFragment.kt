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
    private lateinit var binding: AccountBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = AccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val authUi = view.context.component.authUi
        val presenter = accountViewModel.accountPresenter

        val onSignIn = SignInHandler(activity!!, authUi)
        val onShare = ShareHandler(view.context)
        val onOss = OssHandler(view.context)

        viewLifecycleOwner.lifecycleScope.launch {
            val binder = AccountUiBinder(binding, onSignIn, onShare, onOss, presenter.events)

            binder.bindTo(presenter)
        }
    }
}

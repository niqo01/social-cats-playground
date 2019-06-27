package com.nicolasmilliard.socialcats.account.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.nicolasmilliard.presentation.bindTo
import com.nicolasmilliard.socialcats.component
import kotlinx.coroutines.launch

class AccountFragment : Fragment(R.layout.account) {

    private val accountViewModel: AccountViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val authUi = view.context.component.authUi
        val presenter = accountViewModel.accountPresenter

        val onSignIn = SignInHandler(activity!!, authUi)
        val onShare = ShareHandler(view.context)

        viewLifecycleOwner.lifecycleScope.launch {
            val binder = AccountUiBinder(view, onSignIn, onShare, presenter.events)

            binder.bindTo(presenter)
        }
    }
}

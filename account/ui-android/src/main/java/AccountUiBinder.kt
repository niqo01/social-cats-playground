package com.nicolasmilliard.socialcats.account.ui

import androidx.core.view.isVisible
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import com.nicolasmilliard.presentation.UiBinder
import com.nicolasmilliard.socialcats.account.presenter.AccountPresenter.Event
import com.nicolasmilliard.socialcats.account.presenter.AccountPresenter.Event.ClearErrorStatus
import com.nicolasmilliard.socialcats.account.presenter.AccountPresenter.Model
import com.nicolasmilliard.socialcats.account.presenter.AccountPresenter.Model.LoadingStatus
import com.nicolasmilliard.socialcats.account.presenter.AccountPresenter.Model.ProcessingStatus.FAILED_DELETE_ACCOUNT
import com.nicolasmilliard.socialcats.account.presenter.AccountPresenter.Model.ProcessingStatus.FAILED_SIGN_OUT
import com.nicolasmilliard.socialcats.account.presenter.AccountPresenter.Model.ProcessingStatus.PROCESSING
import com.nicolasmilliard.socialcats.account.ui.databinding.AccountBinding
import com.nicolasmilliard.socialcats.session.SessionState.NoSession
import com.nicolasmilliard.socialcats.session.SessionState.Session

class AccountUiBinder(
    private val binding: AccountBinding,
    private val onSignInClick: SignInHandler,
    private val onShareClick: ShareHandler,
    private val onOssClick: OssHandler,
    private val events: (Event) -> Unit
) : UiBinder<Model> {

    private var snackbar: Snackbar? = null

    init {
        binding.apply {
            deleteAccount.setOnClickListener {
                events(Event.DeleteAccount)
            }
            share.setOnClickListener {
                events(ClearErrorStatus)
                onShareClick()
            }
        }
        binding.oss.setOnClickListener {
            events(ClearErrorStatus)
            onOssClick()
        }
    }

    override fun bind(model: Model, oldModel: Model?) {

        binding.auth.isEnabled = model.processingStatus != PROCESSING
        binding.deleteAccount.isEnabled = model.processingStatus != PROCESSING
        binding.share.isEnabled = model.processingStatus != PROCESSING
        binding.linkAccount.isEnabled = model.processingStatus != PROCESSING

        binding.linkAccount.isVisible = model.showLinkAccount

        when (model.sessionState) {
            is NoSession -> {
                binding.deleteAccount.isVisible = false
                binding.auth.apply {
                    text = resources.getString(R.string.sign_in)
                    setOnClickListener {
                        events(ClearErrorStatus)
                        onSignInClick()
                    }
                }
            }
            is Session -> {
                binding.deleteAccount.isVisible = true
                binding.auth.apply {
                    text = resources.getString(R.string.sign_out)
                    setOnClickListener {
                        events(Event.SignOut)
                    }
                    (model.sessionState as Session).user.apply {
                        binding.name.text = name
                    }
                }
            }
        }

        val message = when {
            model.loadingStatus == LoadingStatus.FAILED -> R.string.loading_account_failed
            model.processingStatus == FAILED_SIGN_OUT -> R.string.sign_out_failed
            model.processingStatus == FAILED_DELETE_ACCOUNT -> R.string.delete_account_failed
            else -> null
        }

        if (message != null) {

            var snackbar = this.snackbar
            if (snackbar == null) {
                snackbar = Snackbar.make(binding.root, message, LENGTH_INDEFINITE)
                snackbar.show()
                this.snackbar = snackbar
            } else {
                snackbar.setText(message)
            }

            snackbar.setAction(R.string.action_dismiss) {
                events(ClearErrorStatus)
            }
        } else {
            snackbar?.dismiss()
        }
    }
}

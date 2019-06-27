package com.nicolasmilliard.socialcats.account.ui

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
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
import com.nicolasmilliard.socialcats.session.SessionState.NoSession
import com.nicolasmilliard.socialcats.session.SessionState.Session
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class AccountUiBinder(
    private val view: View,
    private val onSignInClick: SignInHandler,
    private val onShareClick: ShareHandler,
    private val events: (Event) -> Unit
) : UiBinder<Model> {

    private val resources = view.resources

    private val userPhoto: ImageView = view.findViewById(R.id.photo)
    private val userName: TextView = view.findViewById(R.id.name)
    private val authButton: Button = view.findViewById(R.id.auth)
    private val deleteAccountButton: Button = view.findViewById(R.id.delete_account)
    private val shareButton: Button = view.findViewById(R.id.share)

    private var snackbar: Snackbar? = null

    init {
        deleteAccountButton.setOnClickListener {
            events(Event.DeleteAccount)
        }
        shareButton.setOnClickListener {
            onShareClick()
        }
    }

    override fun bind(model: Model, oldModel: Model?) {

        authButton.isEnabled = model.processingStatus != Model.ProcessingStatus.PROCESSING
        deleteAccountButton.isEnabled = model.processingStatus != PROCESSING
        shareButton.isEnabled = model.processingStatus != PROCESSING

        when (model.sessionState) {
            is NoSession -> {
                authButton.text = resources.getString(R.string.sign_in)
                authButton.setOnClickListener {
                    onSignInClick()
                }
            }
            is Session -> {
                authButton.text = resources.getString(R.string.sign_out)
                authButton.setOnClickListener {
                    events(Event.SignOut)
                }
                (model.sessionState as Session).user.apply {
                    userName.text = name
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
                snackbar = Snackbar.make(view, message, LENGTH_INDEFINITE)
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

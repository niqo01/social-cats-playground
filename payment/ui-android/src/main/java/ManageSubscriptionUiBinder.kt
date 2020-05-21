package com.nicolasmilliard.socialcats.payment.ui

import android.view.ViewStub
import androidx.core.view.isInvisible
import com.google.android.material.snackbar.Snackbar
import com.nicolasmilliard.presentation.UiBinder
import com.nicolasmilliard.socialcats.payment.Price
import com.nicolasmilliard.socialcats.payment.presenter.ManageSubscriptionPresenter
import com.nicolasmilliard.socialcats.payment.presenter.ManageSubscriptionPresenter.Event
import com.nicolasmilliard.socialcats.payment.presenter.ManageSubscriptionPresenter.Model
import com.nicolasmilliard.socialcats.payment.ui.databinding.ManageSubscriptionBinding
import com.nicolasmilliard.socialcats.payment.ui.new.NewSubscriptionHandler
import com.nicolasmilliard.socialcats.ui.CheckConnectivityHandler
import com.nicolasmilliard.socialcats.ui.NoConnectionLayout
import timber.log.Timber

class ManageSubscriptionUiBinder(
    private val binding: ManageSubscriptionBinding,
    private val events: (Event) -> Unit,
    private val onCheckConnectivityClick: CheckConnectivityHandler,
    onNewSubscriptionClick: NewSubscriptionHandler
) : UiBinder<Model> {

    private val context = binding.root.context
    private val resources = binding.root.resources

    private var snackbar: Snackbar? = null

    private var prices: List<Price> = emptyList()

    init {
        binding.cancelButton.setOnClickListener {
            events(Event.CancelClick)
        }
        binding.newButton.setOnClickListener {
            onNewSubscriptionClick()
        }
    }

    override fun bind(model: Model, oldModel: Model?) {
        Timber.i("Payment UI binder bind: $model")
        binding.cancelButton.isEnabled = model.isSubscribed
        binding.newButton.isEnabled = !model.isSubscribed
        when {
            model.isLoading -> {
                binding.content.displayedChildId = R.id.loading
            }
            model.authToken == null -> {
                binding.content.displayedChildId = R.id.unAuth
            }
            !model.isLoading && model.noConnection -> {
                val viewStub: ViewStub? =
                    binding.content.findViewById(R.id.no_connection_stub)
                viewStub?.inflate()
                val noConnectionLayout: NoConnectionLayout =
                    binding.content.findViewById(R.id.no_connection)
                binding.content.displayedChildId = R.id.no_connection
                noConnectionLayout.setOnClickListener { events(ManageSubscriptionPresenter.Event.Retry) }
                noConnectionLayout.connectionSettingsButton.apply {
                    setOnClickListener { onCheckConnectivityClick() }
                    isInvisible = model.hasConnectivity
                }
            }
            !model.isLoading -> {
                binding.content.displayedChildId = R.id.payment_content
//                binding.payButton.isEnabled = binding.cardInput.valid
            }
        }
    }
}

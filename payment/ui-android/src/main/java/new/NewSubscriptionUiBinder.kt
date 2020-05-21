package com.nicolasmilliard.socialcats.payment.ui.new

import android.view.ViewStub
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS
import androidx.core.content.getSystemService
import androidx.core.view.isInvisible
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import com.nicolasmilliard.presentation.UiBinder
import com.nicolasmilliard.socialcats.payment.Price
import com.nicolasmilliard.socialcats.payment.presenter.NewSubscriptionPresenter.Event
import com.nicolasmilliard.socialcats.payment.presenter.NewSubscriptionPresenter.Model
import com.nicolasmilliard.socialcats.payment.ui.R
import com.nicolasmilliard.socialcats.payment.ui.databinding.NewSubscriptionBinding
import com.nicolasmilliard.socialcats.ui.CheckConnectivityHandler
import com.nicolasmilliard.socialcats.ui.NoConnectionLayout
import timber.log.Timber

class NewSubscriptionUiBinder(
    private val binding: NewSubscriptionBinding,
    private val events: (Event) -> Unit,
    private val onCheckConnectivityClick: CheckConnectivityHandler,
    private val onRequirePaymentConfirmation: ConfirmPaymentHandler
) : UiBinder<Model> {

    private val context = binding.root.context
    private val resources = binding.root.resources

    private var snackbar: Snackbar? = null

    private var prices: List<Price> = emptyList()

    init {

        binding.payButton.setOnClickListener {
            val paymentMethodCard = binding.cardInput.paymentMethodCard
            if (paymentMethodCard != null) {
                val inputMethodManager = context.getSystemService<InputMethodManager>()!!
                inputMethodManager.hideSoftInputFromWindow(binding.cardInput.windowToken, HIDE_NOT_ALWAYS)
                events(Event.PayClick(prices[0].id, paymentMethodCard))
            } else {
                // TODO
            }
        }
    }

    override fun bind(model: Model, oldModel: Model?) {
        Timber.i("Payment UI binder bind: $model")
        prices = model.prices

        if (model.requireConfirmation != null) {
            onRequirePaymentConfirmation(
                model.requireConfirmation!!.selectedPaymentMethodId,
                model.requireConfirmation!!.clientSecret
            )
        }
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
                noConnectionLayout.setOnClickListener { events(Event.Retry) }
                noConnectionLayout.connectionSettingsButton.apply {
                    setOnClickListener { onCheckConnectivityClick() }
                    isInvisible = model.hasConnectivity
                }
            }
            !model.isLoading -> {
                binding.content.displayedChildId = R.id.payment_content
//                binding.payButton.isEnabled = binding.cardInput.valid
            }
            else -> {
                throw IllegalStateException("Hum, game over")
            }
        }

        var snackbar = this.snackbar
        if (model.stripeErrorCode != null) {
            val msgRes = model.stripeErrorCode!!.toStripeErrorRes()
            if (snackbar == null) {
                snackbar = Snackbar.make(binding.root, msgRes, LENGTH_INDEFINITE)
                snackbar.setAction(R.string.dismiss) {
                    events(Event.ClearStripeError)
                }
                this.snackbar = snackbar
            } else {
                snackbar.setText(msgRes)
            }
            snackbar.show()
        } else {
            snackbar?.dismiss()
        }
    }

    private fun String.toStripeErrorRes(): Int = when (this) {
        "invalid_expiry_year" -> com.stripe.android.R.string.invalid_expiry_year
        "invalid_zip" -> com.stripe.android.R.string.invalid_zip
        "invalid_card_number" -> com.stripe.android.R.string.invalid_card_number
        "invalid_cvc" -> com.stripe.android.R.string.invalid_cvc
        "invalid_expiry_month" -> com.stripe.android.R.string.invalid_expiry_month
        else -> R.string.unknown_card_error
    }
}

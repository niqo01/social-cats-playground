package checkout

import androidx.lifecycle.ViewModel
import com.nicolasmilliard.socialcats.payment.presenter.NewSubscriptionPresenter
import timber.log.Timber

class NewSubscriptionViewModel(val presenter: NewSubscriptionPresenter) : ViewModel() {

    override fun onCleared() {
        super.onCleared()
        Timber.i("onCleared")
    }
}

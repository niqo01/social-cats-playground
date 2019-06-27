package com.nicolasmilliard.socialcats.search.ui

import android.view.KeyEvent
import android.view.ViewConfiguration
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS
import androidx.annotation.IdRes
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.getSystemService
import androidx.core.view.forEach
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import com.nicolasmilliard.presentation.UiBinder
import com.nicolasmilliard.socialcats.model.User
import com.nicolasmilliard.socialcats.search.presenter.SearchPresenter
import com.nicolasmilliard.socialcats.search.presenter.SearchPresenter.Event
import com.nicolasmilliard.socialcats.search.presenter.SearchPresenter.Event.ClearRefreshStatus
import com.nicolasmilliard.socialcats.search.presenter.SearchPresenter.Model.LoadingState.Failed
import com.nicolasmilliard.socialcats.search.presenter.SearchPresenter.Model.LoadingState.Loading
import com.nicolasmilliard.socialcats.search.presenter.SearchPresenter.Model.LoadingState.Success
import com.nicolasmilliard.socialcats.search.presenter.SearchPresenter.Model.RefreshState.FAILED
import com.nicolasmilliard.socialcats.search.presenter.SearchPresenter.Model.RefreshState.IDLE
import com.nicolasmilliard.socialcats.search.presenter.SearchPresenter.Model.RefreshState.LOADING
import com.nicolasmilliard.socialcats.search.ui.databinding.SearchBinding
import com.nicolasmilliard.socialcats.ui.CheckConnectivityHandler
import com.nicolasmilliard.socialcats.ui.NoConnectionLayout
import com.nicolasmilliard.socialcats.ui.util.layoutInflater
import com.nicolasmilliard.socialcats.ui.util.onEditorAction
import com.nicolasmilliard.socialcats.ui.util.onKey
import com.nicolasmilliard.socialcats.ui.util.onScroll
import com.nicolasmilliard.socialcats.ui.util.onTextChanged
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class SearchUiBinder(
    private val binding: SearchBinding,
    private val events: (Event) -> Unit,
    private val onUserClick: UserHandler,
    private val onCheckConnectivityClick: CheckConnectivityHandler
) : UiBinder<SearchPresenter.Model> {

    private val context = binding.root.context
    private val resources = binding.root.resources

    private val resultsAdapter =
        UserResultAdapter(context.layoutInflater, object : UserResultAdapter.Callback {
            override fun onUserClicked(user: User) = onUserClick(user)
        })

    private var snackbar: Snackbar? = null

    init {
        binding.results.adapter = resultsAdapter

        val layoutManager = LinearLayoutManager(context)
        binding.results.layoutManager = layoutManager
        binding.results.addItemDecoration(DividerItemDecoration(context, layoutManager.orientation))

        TooltipCompat.setTooltipText(binding.clearQuery, binding.clearQuery.contentDescription)

        binding.clearQuery.setOnClickListener {
            binding.query.setText("")
        }

        binding.query.apply {

            onTextChanged {
                isVisible = it.isNotEmpty()

                events(Event.QueryChanged(it.toString()))
            }

            onKey {
                if (it.keyCode == KeyEvent.KEYCODE_ENTER) {
                    resultsAdapter.invokeFirstItem()
                    true
                } else {
                    false
                }
            }
            onEditorAction {
                if (it == EditorInfo.IME_ACTION_GO) {
                    resultsAdapter.invokeFirstItem()
                    true
                } else {
                    false
                }
            }
        }

        val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
        var totalDy = 0
        binding.results.onScroll { _, dy ->
            if (dy > 0) {
                totalDy += dy
                if (totalDy >= touchSlop) {
                    totalDy = 0

                    val inputMethodManager = context.getSystemService<InputMethodManager>()!!
                    inputMethodManager.hideSoftInputFromWindow(
                        binding.query.windowToken,
                        HIDE_NOT_ALWAYS
                    )
                }
            }
        }
    }

    override fun bind(model: SearchPresenter.Model, oldModel: SearchPresenter.Model?) {
        logger.info { "Search UI binder bind: $model" }

        when (model.loadingState) {
            is Failed -> {
                displayContentChildId(R.id.no_connection)
                val noConnectionLayout =
                    binding.content.findViewById<NoConnectionLayout>(R.id.no_connection)
                noConnectionLayout.setOnClickListener { events(Event.Retry) }
                noConnectionLayout.connectionSettingsButton.apply {
                    setOnClickListener { onCheckConnectivityClick() }
                    isInvisible = model.isConnected
                }
            }
            is Loading -> displayContentChildId(R.id.progress)
            is Success -> {
                val success = model.loadingState as Success
                val count = success.count
                binding.query.hint =
                    resources.getQuantityString(R.plurals.search_classes, count.toInt(), count)

                val queryResults = success.queryResults
                val itemResults = queryResults.items.map { UserResult(queryResults.query, it) }
                resultsAdapter.submitList(itemResults) {
                    // Always reset the scroll position to the top when the query changes.
                    binding.results.scrollToPosition(0)
                }
                displayContentChildId(if (count == 0L) R.id.no_content else R.id.results)

                if (success.refreshing != IDLE) {
                    val message =
                        if (success.refreshing == LOADING) R.string.updating else R.string.updating_failed

                    var snackbar = this.snackbar
                    if (snackbar == null) {
                        snackbar = Snackbar.make(binding.results, message, LENGTH_INDEFINITE)
                        snackbar.show()
                        this.snackbar = snackbar
                    } else {
                        snackbar.setText(message)
                    }

                    if (success.refreshing == FAILED) {
                        snackbar.setAction(R.string.dismiss) {
                            events(ClearRefreshStatus)
                        }
                    }
                } else {
                    snackbar?.dismiss()
                }
            }
        }
    }

    private fun displayContentChildId(@IdRes id: Int) {
        binding.content.forEach {
            it.isVisible = it.id == id
        }
    }
}

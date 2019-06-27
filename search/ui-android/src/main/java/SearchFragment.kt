package com.nicolasmilliard.socialcats.search.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.nicolasmilliard.presentation.bindTo
import com.nicolasmilliard.socialcats.ui.CHECK_CONNECTIVITY_SETTINGS_CODE
import com.nicolasmilliard.socialcats.ui.CheckConnectivityHandler
import kotlinx.coroutines.launch
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class SearchFragment : Fragment(R.layout.search) {

    private val searchViewModel: SearchViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val presenter = searchViewModel.searchPresenter

        val onItemUserClick = OpenProfileUserHandler(view.context)
        val onCheckConnectivityClick = CheckConnectivityHandler(activity!!, CHECK_CONNECTIVITY_SETTINGS_CODE)

        viewLifecycleOwner.lifecycleScope.launch {
            val binder = SearchUiBinder(view, presenter.events, onItemUserClick, onCheckConnectivityClick)

            binder.bindTo(presenter)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHECK_CONNECTIVITY_SETTINGS_CODE) {
            logger.info { "Check connectivity result: $resultCode" }
        }
    }
}

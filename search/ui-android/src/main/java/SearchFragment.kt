package com.nicolasmilliard.socialcats.search.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.nicolasmilliard.presentation.bindTo
import com.nicolasmilliard.socialcats.search.ui.databinding.SearchBinding
import com.nicolasmilliard.socialcats.ui.CHECK_CONNECTIVITY_SETTINGS_CODE
import com.nicolasmilliard.socialcats.ui.CheckConnectivityHandler
import kotlinx.coroutines.launch
import timber.log.Timber

class SearchFragment : Fragment() {

    private val searchViewModel: SearchViewModel by viewModels()
    private lateinit var binding: SearchBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = SearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val presenter = searchViewModel.searchPresenter

        val onItemUserClick = OpenProfileUserHandler(view.context)
        val onCheckConnectivityClick = CheckConnectivityHandler(activity!!, CHECK_CONNECTIVITY_SETTINGS_CODE)

        viewLifecycleOwner.lifecycleScope.launch {
            val binder = SearchUiBinder(binding, presenter.events, onItemUserClick, onCheckConnectivityClick)

            binder.bindTo(presenter)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHECK_CONNECTIVITY_SETTINGS_CODE) {
            Timber.i("Check connectivity result: $resultCode")
        }
    }
}

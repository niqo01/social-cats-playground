package com.nicolasmilliard.socialcats.search.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.nicolasmilliard.presentation.bindTo
import com.nicolasmilliard.socialcats.component
import com.nicolasmilliard.socialcats.search.ui.databinding.SearchBinding
import com.nicolasmilliard.socialcats.ui.CHECK_CONNECTIVITY_SETTINGS_CODE
import com.nicolasmilliard.socialcats.ui.CheckConnectivityHandler
import kotlinx.coroutines.launch
import timber.log.Timber

class SearchFragment : Fragment() {

    private val searchViewModel: SearchViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val presenter = searchViewModel.searchPresenter
        val imageLoader = requireContext().component.imageLoader

        val onItemUserClick = OpenProfileUserHandler(findNavController())
        val onCheckConnectivityClick = CheckConnectivityHandler(requireActivity(), CHECK_CONNECTIVITY_SETTINGS_CODE)

        val binding = SearchBinding.inflate(inflater, container, false)
        viewLifecycleOwner.lifecycleScope.launch {
            val binder = SearchUiBinder(binding, presenter.events, onItemUserClick, onCheckConnectivityClick, imageLoader)

            binder.bindTo(presenter)
        }
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHECK_CONNECTIVITY_SETTINGS_CODE) {
            Timber.i("Check connectivity result: $resultCode")
        }
    }
}

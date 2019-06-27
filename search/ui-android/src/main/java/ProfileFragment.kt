package com.nicolasmilliard.socialcats.search.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.nicolasmilliard.socialcats.search.ui.databinding.ProfileBinding

class ProfileFragment : Fragment() {

    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = ProfileBinding.inflate(inflater, container, false)
        return binding!!.root
    }
}

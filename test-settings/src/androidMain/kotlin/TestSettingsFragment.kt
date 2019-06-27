package com.nicolasmilliard.socialcats.testsettings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nicolasmilliard.socialcats.testsettings.databinding.FragmentTestsettingsBinding

class TestSettingsFragment : androidx.fragment.app.Fragment() {

    interface TestSettingsListener {
        fun onFeatureToggleClicked()
        fun onTestSettingClicked()
    }
    var testSettingListener: TestSettingsListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentTestsettingsBinding.inflate(inflater, container, false)
        binding.textviewTestsettingsFeaturetoggle.setOnClickListener { testSettingListener?.onFeatureToggleClicked() }
        binding.formattextviewTestsettingsTestsetting.setOnClickListener { testSettingListener?.onTestSettingClicked() }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        activity?.title = "Test Settings"
    }
}

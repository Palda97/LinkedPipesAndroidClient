package cz.palda97.lpclient.view.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.FragmentSettingsBinding
import cz.palda97.lpclient.viewmodel.SettingsViewModel

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var viewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)
        val root = binding.root
        viewModel = ViewModelProviders.of(this).get(SettingsViewModel::class.java)
        setUpComponents()
        return root
    }

    private fun setUpComponents() {
        binding.notificationSwitch.isChecked = viewModel.notifications
        binding.notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.notifications = isChecked
        }
    }
}
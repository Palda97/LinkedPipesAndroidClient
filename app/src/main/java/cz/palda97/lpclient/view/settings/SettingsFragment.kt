package cz.palda97.lpclient.view.settings

import android.os.Bundle
import android.util.Log
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
    private lateinit var serverRecyclerAdapter: ServerRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)
        val root = binding.root
        viewModel = ViewModelProviders.of(this).get(SettingsViewModel::class.java)
        setUpComponents()
        setUpObservers()
        return root
    }

    private fun setUpComponents() {
        fun setUpNotificationSwitch() {
            binding.notificationSwitch.isChecked = viewModel.notifications
            binding.notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
                viewModel.notifications = isChecked
            }
        }
        fun setUpServerRecycler() {
            serverRecyclerAdapter = ServerRecyclerAdapter(
                {viewModel.editServer(it)},
                {viewModel.deleteServer(it)}
            )
            binding.insertServerInstancesHere.adapter = serverRecyclerAdapter
        }
        setUpNotificationSwitch()
        setUpServerRecycler()
    }

    private fun setUpObservers() {
        viewModel.liveServers.observe(viewLifecycleOwner, Observer {
            if(it == null)
                return@Observer
            if(it.isOk) {
                Log.d(TAG, "it.isOk")
                serverRecyclerAdapter.updateServerList(it.mailContent!!)
            }
            binding.mail = it
            binding.executePendingBindings()
        })
    }

    companion object {
        private const val TAG = "SettingsFragment"
    }
}
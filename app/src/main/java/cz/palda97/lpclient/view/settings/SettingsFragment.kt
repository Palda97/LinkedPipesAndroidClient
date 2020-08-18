package cz.palda97.lpclient.view.settings

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.FragmentSettingsBinding
import cz.palda97.lpclient.model.ServerInstance
import cz.palda97.lpclient.view.EditServerActivity
import cz.palda97.lpclient.viewmodel.SettingsViewModel

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var viewModel: SettingsViewModel
    private lateinit var serverRecyclerAdapter: ServerRecyclerAdapter
    private lateinit var fab: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)
        val root = binding.root
        viewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
        setUpComponents()
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
                { viewModel.editServer(it) },
                { viewModel.deleteServer(it) }
            )
            binding.insertServerInstancesHere.adapter = serverRecyclerAdapter
            viewModel.liveServers.observe(viewLifecycleOwner, Observer {
                if (it == null)
                    return@Observer
                if (it.isOk) {
                    Log.d(TAG, "it.isOk")
                    Log.d(TAG, "item count: ${it.mailContent!!.size}")
                    serverRecyclerAdapter.updateServerList(it.mailContent!!)
                }
                binding.mail = it
                binding.executePendingBindings()
            })
        }

        fun setUpFAB() {
            fab = binding.fab
            fab.setOnClickListener {
                addServer()
            }
        }
        setUpNotificationSwitch()
        setUpServerRecycler()
        setUpFAB()
    }

    private fun addServer() {
        viewModel.editServer(ServerInstance())
        EditServerActivity.start(requireActivity())
    }

    companion object {
        private const val TAG = "SettingsFragment"
    }
}
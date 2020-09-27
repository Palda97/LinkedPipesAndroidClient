package cz.palda97.lpclient.view.settings

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
import com.google.android.material.snackbar.Snackbar
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.FragmentSettingsBinding
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.view.EditServerActivity
import cz.palda97.lpclient.view.MainActivity
import cz.palda97.lpclient.view.RecyclerViewCosmetics
import cz.palda97.lpclient.viewmodel.settings.SettingsViewModel


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
        //tmpButtons()
        return root
    }

    /*private fun tmpButtons() {
        binding.tmpButtonDeleteAllInstances.setOnClickListener {
            viewModel.deleteAllInstances()
        }
        binding.tmpButtonAddSomeInstances.setOnClickListener {
            val list = listOf(
                ServerInstance("Home server", "192.168.1.10"),
                ServerInstance("Work server", "10.0.42.111"),
                ServerInstance("Test server", "192.168.1.11")
            )
            list.forEach {
                viewModel.forceSaveServer(it)
            }
        }
    }*/

    private fun setUpComponents() {
        fun setUpNotificationSwitch() {
            binding.notificationSwitch.isChecked = viewModel.notifications
            binding.notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
                viewModel.notifications = isChecked
            }
        }

        fun setUpServerRecycler() {
            serverRecyclerAdapter = ServerRecyclerAdapter { editServer(it) }
            RecyclerViewCosmetics.makeItAllWork(
                binding.insertServerInstancesHere,
                serverRecyclerAdapter,
                { deleteServer(it) },
                requireContext()
            )
            viewModel.liveServers.observe(viewLifecycleOwner, Observer {
                if (it == null)
                    return@Observer
                if (it.isOk) {
                    it.mailContent!!
                    Log.d(TAG, "it.isOk")
                    Log.d(TAG, "item count: ${it.mailContent.size}")

                    it.mailContent.forEach {
                        with(it) {
                            Log.d(TAG, "name: $name\nurl: $url\nid: $id")
                        }
                    }
                    serverRecyclerAdapter.updateServerList(it.mailContent)
                    binding.noInstances = it.mailContent.isEmpty()
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
        viewModel.addServer()
        EditServerActivity.start(requireActivity())
    }

    private fun editServer(serverInstance: ServerInstance) {
        viewModel.editServer(serverInstance)
        EditServerActivity.start(requireActivity())
    }

    private fun deleteServer(serverInstance: ServerInstance) {
        l("deleting ${serverInstance.name}")
        Snackbar.make(
            binding.root,
            "${serverInstance.name} ${getString(R.string.has_been_deleted)}",
            Snackbar.LENGTH_LONG
        )
            .setAnchorView(fab)
            .setAction(getString(R.string.undo), View.OnClickListener {
                undoLastDeleteServer()
            })
            .show()
        viewModel.deleteServer(serverInstance)
    }

    private fun undoLastDeleteServer() {
        l("undoing server deletion")
        viewModel.undoLastDeleteServer()
    }

    override fun onResume() {
        super.onResume()
        MainActivity.switchToFragment = null
    }

    companion object {
        private const val TAG = "SettingsFragment"
        private fun l(msg: String) = Log.d(TAG, msg)
    }
}
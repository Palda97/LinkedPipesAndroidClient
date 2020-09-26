package cz.palda97.lpclient.view.executions

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
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.FragmentExecutionsBinding
import cz.palda97.lpclient.view.RecyclerViewCosmetics
import cz.palda97.lpclient.view.ServerDropDownMagic.setUpWithServers
import cz.palda97.lpclient.viewmodel.executions.ExecutionV
import cz.palda97.lpclient.viewmodel.executions.ExecutionsViewModel
import cz.palda97.lpclient.viewmodel.settings.SettingsViewModel
import cz.palda97.lpclient.view.FABCosmetics.hideOrShowSub

class ExecutionsFragment : Fragment() {

    private lateinit var binding: FragmentExecutionsBinding
    private lateinit var viewModel: ExecutionsViewModel
    private lateinit var refreshFab: FloatingActionButton
    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //return inflater.inflate(R.layout.fragment_executions, container, false)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_executions, container, false)
        val root = binding.root
        viewModel = ViewModelProvider(this).get(ExecutionsViewModel::class.java)
        settingsViewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
        setUpComponents()
        return root
    }

    private fun setUpComponents() {
        fun setUpRefreshFab() {
            refreshFab = binding.fabRefresh.apply {
                hideOrShowSub(viewModel.liveExecutions, viewLifecycleOwner)
                setOnClickListener {
                    refreshExecutions()
                }
            }
        }

        fun setUpDropDown() {
            binding.serverInstanceDropDown.setUpWithServers(
                requireContext(),
                settingsViewModel,
                viewLifecycleOwner,
                { viewModel.setServerToFilterFun(it) },
                viewModel.serverToFilter
            )
        }

        fun setUpExecutionRecycler() {
            val executionRecycleAdapter = ExecutionRecycleAdapter(
                { viewExecution(it) },
                { launchExecution(it) }
            )
            RecyclerViewCosmetics.makeItAllWork(
                binding.insertExecutionsHere,
                executionRecycleAdapter,
                { deleteExecution(it) },
                requireContext()
            )
            viewModel.liveExecutions.observe(viewLifecycleOwner, Observer {
                val mail = it ?: return@Observer
                if (mail.isOk) {
                    mail.mailContent!!
                    executionRecycleAdapter.updateExecutionList(mail.mailContent)
                    binding.noInstances = mail.mailContent.isEmpty()
                }
                binding.mail = mail
                binding.executePendingBindings()
            })
            binding.fastscroll.setRecyclerView(binding.insertExecutionsHere)
        }

        setUpRefreshFab()
        setUpDropDown()
        setUpExecutionRecycler()
    }

    private fun refreshExecutions() {
        viewModel.refreshExecutionsButton()
    }

    private fun viewExecution(execution: ExecutionV) {
        l("view ${execution.id}")
    }

    private fun launchExecution(execution: ExecutionV) {
        TODO()
    }

    private fun deleteExecution(execution: ExecutionV) {
        TODO()
    }

    companion object {
        private val TAG = Injector.tag(this)
        private fun l(msg: String) = Log.d(TAG, msg)
    }
}
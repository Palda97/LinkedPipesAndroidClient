package cz.palda97.lpclient.view.executions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearSmoothScroller
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.FragmentExecutionsBinding
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.repository.RepositoryRoutines
import cz.palda97.lpclient.view.RecyclerViewCosmetics
import cz.palda97.lpclient.view.ServerDropDownMagic.setUpWithServers
import cz.palda97.lpclient.viewmodel.executions.ExecutionV
import cz.palda97.lpclient.viewmodel.executions.ExecutionsViewModel
import cz.palda97.lpclient.viewmodel.settings.SettingsViewModel
import cz.palda97.lpclient.view.FABCosmetics.hideOrShowSub
import cz.palda97.lpclient.viewmodel.CommonViewModel
import cz.palda97.lpclient.viewmodel.pipelines.PipelinesViewModel

class ExecutionsFragment : Fragment() {

    private lateinit var binding: FragmentExecutionsBinding
    private lateinit var viewModel: ExecutionsViewModel
    private lateinit var refreshFab: FloatingActionButton
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var pipelineViewModel: PipelinesViewModel
    private lateinit var commonViewModel: CommonViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //return inflater.inflate(R.layout.fragment_executions, container, false)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_executions, container, false)
        val root = binding.root
        viewModel = ExecutionsViewModel.getInstance(this)
        settingsViewModel = SettingsViewModel.getInstance(this)
        pipelineViewModel = PipelinesViewModel.getInstance(this)
        commonViewModel = CommonViewModel.getInstance(this)
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
                { commonViewModel.setServerToFilterFun(it) },
                commonViewModel.serverToFilter
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
                    if (mail.msg == ExecutionsViewModel.SCROLL) {
                        val smoothScroller = object : LinearSmoothScroller(requireContext()) {
                            override fun getVerticalSnapPreference(): Int {
                                return SNAP_TO_START
                            }
                        }
                        smoothScroller.targetPosition = 0
                        binding.insertExecutionsHere.layoutManager?.startSmoothScroll(smoothScroller)
                    }
                }
                binding.mail = if (mail.isError) {
                    MailPackage.error(when(mail.msg) {
                        RepositoryRoutines.SERVER_NOT_FOUND -> getString(R.string.server_instance_no_longer_registered)
                        RepositoryRoutines.INTERNAL_ERROR -> getString(R.string.internal_error)
                        else -> "${getString(R.string.error_while_getting_executions_from)} ${mail.msg}"
                    })
                } else {
                    mail
                }
                binding.executePendingBindings()
            })
            binding.fastscroll.setRecyclerView(binding.insertExecutionsHere)
        }

        fun setUpLaunchStatus() {
            pipelineViewModel.launchStatus.observe(viewLifecycleOwner, Observer {
                if (it == null || it == PipelinesViewModel.LaunchStatus.WAITING)
                    return@Observer
                pipelineViewModel.resetLaunchStatus()
                val text: String = when (it) {
                    PipelinesViewModel.LaunchStatus.PIPELINE_NOT_FOUND -> getString(R.string.pipeline_not_found)
                    PipelinesViewModel.LaunchStatus.SERVER_NOT_FOUND -> getString(R.string.server_not_found)
                    PipelinesViewModel.LaunchStatus.CAN_NOT_CONNECT -> getString(R.string.can_not_connect_to_server)
                    PipelinesViewModel.LaunchStatus.INTERNAL_ERROR -> getString(R.string.internal_error)
                    PipelinesViewModel.LaunchStatus.SERVER_ERROR -> getString(R.string.server_side_error)
                    PipelinesViewModel.LaunchStatus.WAITING -> ""
                    PipelinesViewModel.LaunchStatus.OK -> {
                        refreshExecutions(true)
                        getString(R.string.successfully_launched)
                    }
                    PipelinesViewModel.LaunchStatus.PROTOCOL_PROBLEM -> getString(R.string.problem_with_protocol)
                }
                Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG)
                    .setAnchorView(refreshFab)
                    .show()
            })
        }

        setUpRefreshFab()
        setUpDropDown()
        setUpExecutionRecycler()
        setUpLaunchStatus()
    }

    private fun refreshExecutions(silent: Boolean = false) {
        return when(silent) {
            true -> viewModel.silentRefresh()
            false -> viewModel.refreshExecutionsButton()
        }
    }

    private fun viewExecution(execution: ExecutionV) {
        l("view ${execution.id}")
    }

    private fun launchExecution(execution: ExecutionV) {
        pipelineViewModel.launchPipeline(execution)
    }

    private fun deleteExecution(execution: ExecutionV) {
        viewModel.deleteExecution(execution)
        Snackbar.make(
            binding.root,
            "${execution.pipelineName} ${getString(R.string.has_been_deleted)}",
            Snackbar.LENGTH_LONG
        )
            .setAnchorView(refreshFab)
            .setAction(getString(R.string.undo)) {
                viewModel.cancelDeletion(execution)
            }
            .show()
    }

    companion object {
        private val l = Injector.generateLogFunction(this)
    }
}
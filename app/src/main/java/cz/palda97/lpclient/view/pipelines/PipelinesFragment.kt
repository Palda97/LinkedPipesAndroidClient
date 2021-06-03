package cz.palda97.lpclient.view.pipelines

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.FragmentPipelinesBinding
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.view.RecyclerViewCosmetics
import cz.palda97.lpclient.model.entities.pipelineview.PipelineView
import cz.palda97.lpclient.model.repository.PipelineRepository
import cz.palda97.lpclient.model.repository.RepositoryRoutines
import cz.palda97.lpclient.view.EditPipelineActivity
import cz.palda97.lpclient.view.FABCosmetics.hideOrShowSub
import cz.palda97.lpclient.viewmodel.pipelines.PipelinesViewModel
import cz.palda97.lpclient.viewmodel.settings.SettingsViewModel
import cz.palda97.lpclient.view.ServerDropDownMagic.setUpWithServers
import cz.palda97.lpclient.view.editpipeline.CreatePipelineDialog
import cz.palda97.lpclient.viewmodel.CommonViewModel

/**
 * Fragment for displaying pipelineViews.
 */
class PipelinesFragment : Fragment() {

    private lateinit var binding: FragmentPipelinesBinding
    private lateinit var fab: FloatingActionButton
    private lateinit var refreshFab: FloatingActionButton
    private lateinit var viewModel: PipelinesViewModel
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var commonViewModel: CommonViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_pipelines, container, false)
        val root = binding.root
        viewModel = PipelinesViewModel.getInstance(this)
        settingsViewModel = SettingsViewModel.getInstance(this)
        commonViewModel = CommonViewModel.getInstance(this)
        setUpComponents()
        return root
    }

    private val PipelineRepository.CacheStatus.newPipelineErrorMessage: String
        get() = when(this) {
            PipelineRepository.CacheStatus.SERVER_NOT_FOUND -> getString(R.string.can_not_connect_to_server)
            PipelineRepository.CacheStatus.DOWNLOAD_ERROR -> getString(R.string.error_while_downloading_response)
            PipelineRepository.CacheStatus.PARSING_ERROR -> getString(R.string.error_while_parsing_response)
            PipelineRepository.CacheStatus.NO_PIPELINE_TO_LOAD -> getString(R.string.internal_error)
            PipelineRepository.CacheStatus.INTERNAL_ERROR -> getString(R.string.internal_error)
        }

    private fun setUpComponents() {

        fun setUpNoServerWarning() {
            settingsViewModel.liveServers.observe(viewLifecycleOwner, Observer {
                val servers = it?.mailContent ?: return@Observer
                binding.noServer = servers.isEmpty()
            })
        }

        fun setUpFAB() {
            fab = binding.fab
            fab.setOnClickListener {
                createPipeline()
            }
        }

        fun setUpRefreshFAB() {
            refreshFab = binding.fabRefresh.apply {
                hideOrShowSub(viewModel.livePipelineViews, viewLifecycleOwner)
                setOnClickListener {
                    refreshPipelines()
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

        fun setUpPipelineRecycler() {
            val pipelineRecyclerAdapter = PipelineRecyclerAdapter(
                { editPipeline(it) },
                { launchPipeline(it) }
            )
            RecyclerViewCosmetics.makeItAllWork(
                binding.insertPipelinesHere,
                pipelineRecyclerAdapter,
                { deletePipeline(it) },
                requireContext()
            )
            viewModel.livePipelineViews.observe(viewLifecycleOwner, Observer {
                val mail = it ?: return@Observer
                if (mail.isOk) {
                    mail.mailContent!!
                    l("it.isOk")
                    l("item count: ${mail.mailContent.size}")
                    //mail.mailContent.forEach { l(it.toString()) }
                    pipelineRecyclerAdapter.updatePipelineList(mail.mailContent)
                    binding.noInstances = mail.mailContent.isEmpty()
                }
                binding.mail = if (mail.isError) {
                    MailPackage.error(when(mail.msg) {
                        RepositoryRoutines.SERVER_NOT_FOUND -> getString(R.string.server_instance_no_longer_registered)
                        RepositoryRoutines.INTERNAL_ERROR -> getString(R.string.internal_error)
                        else -> "${getString(R.string.error_while_getting_pipelines_from)} ${mail.msg}"
                    })
                } else {
                    mail
                }
                binding.executePendingBindings()
                l("livePipelineViews.observe ends")
            })
            binding.fastscroll.setRecyclerView(binding.insertPipelinesHere)
            l("setUpPipelineRecycler ends")
        }

        fun setUpLaunchStatus() {
            viewModel.launchStatus.observe(viewLifecycleOwner, Observer {
                if (it == null || it == PipelinesViewModel.LaunchStatus.WAITING)
                    return@Observer
                viewModel.resetLaunchStatus()
                val text: String = when (it) {
                    PipelinesViewModel.LaunchStatus.PIPELINE_NOT_FOUND -> getString(R.string.pipeline_not_found)
                    PipelinesViewModel.LaunchStatus.SERVER_NOT_FOUND -> getString(R.string.server_not_found)
                    PipelinesViewModel.LaunchStatus.CAN_NOT_CONNECT -> getString(R.string.can_not_connect_to_server)
                    PipelinesViewModel.LaunchStatus.INTERNAL_ERROR -> getString(R.string.internal_error)
                    PipelinesViewModel.LaunchStatus.SERVER_ERROR -> getString(R.string.server_side_error)
                    PipelinesViewModel.LaunchStatus.WAITING -> ""
                    PipelinesViewModel.LaunchStatus.OK -> getString(R.string.successfully_launched)
                    PipelinesViewModel.LaunchStatus.PROTOCOL_PROBLEM -> getString(R.string.problem_with_protocol)
                }
                Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG)
                    .setAnchorView(fab)
                    .show()
            })
        }

        fun setUpNewPipeline() {
            viewModel.liveNewPipeline.observe(viewLifecycleOwner, Observer {
                val pipelineView = when(val res = it ?: return@Observer) {
                    is Either.Left -> {
                        val text = when(res.value) {
                            PipelineRepository.CacheStatus.NO_PIPELINE_TO_LOAD -> {
                                return@Observer
                            }
                            PipelineRepository.CacheStatus.INTERNAL_ERROR -> {
                                //TODO("loading")
                                return@Observer
                            }
                            else -> res.value.newPipelineErrorMessage
                        }
                        Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG)
                            .setAnchorView(fab)
                            .show()
                        return@Observer
                    }
                    is Either.Right -> res.value
                }
                editPipeline(pipelineView, true)
            })
        }

        setUpNoServerWarning()
        setUpFAB()
        setUpRefreshFAB()
        setUpDropDown()
        setUpPipelineRecycler()
        setUpLaunchStatus()
        setUpNewPipeline()
    }

    private fun createPipeline() {
        CreatePipelineDialog.appear(parentFragmentManager)
    }

    private fun refreshPipelines() {
        viewModel.refreshButton()
    }

    private fun editPipeline(pipelineView: PipelineView, isItNewOne: Boolean = false) {
        viewModel.editPipeline(pipelineView, isItNewOne)
        EditPipelineActivity.start(requireContext())
    }

    private fun launchPipeline(pipelineView: PipelineView) {
        viewModel.launchPipeline(pipelineView)
    }

    private fun deletePipeline(pipelineView: PipelineView) {
        viewModel.deletePipeline(pipelineView)
        l("deleting ${pipelineView.prefLabel}")
        Snackbar.make(
            binding.root,
            "${pipelineView.prefLabel} ${getString(R.string.has_been_deleted)}",
            Snackbar.LENGTH_LONG
        )
            .setAnchorView(fab)
            .setAction(getString(R.string.undo)) {
                viewModel.cancelDeletion(pipelineView)
            }
            .show()
    }

    companion object {
        private val l = Injector.generateLogFunction(this)
    }
}
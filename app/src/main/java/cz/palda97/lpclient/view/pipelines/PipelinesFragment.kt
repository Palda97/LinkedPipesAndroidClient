package cz.palda97.lpclient.view.pipelines

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.FragmentPipelinesBinding
import cz.palda97.lpclient.model.ServerInstance
import cz.palda97.lpclient.view.RecyclerViewCosmetics
import cz.palda97.lpclient.model.PipelineView
import cz.palda97.lpclient.viewmodel.pipelines.PipelinesViewModel
import cz.palda97.lpclient.viewmodel.settings.SettingsViewModel

class PipelinesFragment : Fragment() {

    private lateinit var binding: FragmentPipelinesBinding
    private lateinit var fab: FloatingActionButton
    private lateinit var refreshFab: FloatingActionButton
    private lateinit var viewModel: PipelinesViewModel
    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_pipelines, container, false)
        val root = binding.root
        viewModel = ViewModelProvider(this).get(PipelinesViewModel::class.java)
        settingsViewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
        setUpComponents()
        tmpButtons()
        return root
    }

    private fun tmpButtons() {
        //
    }

    private fun setUpComponents() {
        fun setUpFAB() {
            fab = binding.fab
            fab.setOnClickListener {
                createPipeline()
            }
        }

        fun setUpRefreshFAB() {
            refreshFab = binding.fabRefresh
            refreshFab.setOnClickListener {
                refreshPipelines()
            }
        }

        fun setUpDropDown() {
            val adapter = ArrayAdapter<String>(requireContext(), R.layout.dropdown_item_text_view)
            settingsViewModel.activeLiveServers.observe(viewLifecycleOwner, Observer {
                val mail = it ?: return@Observer
                if (!mail.isOk)
                    return@Observer
                mail.mailContent!!
                adapter.clear()
                //adapter.add("")
                adapter.addAll(mail.mailContent.map(ServerInstance::name))
                mail.mailContent.forEach { l(it.toString()) }
                divLog()
                adapter.notifyDataSetChanged()
                binding.serverInstanceDropDown.setAdapter(adapter)
            })
            binding.serverInstanceDropDown.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val server = settingsViewModel.findServerByName(s.toString())
                    l("selected server: ${server?.name}")
                    viewModel.serverToFilter = server
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
            viewModel.serverToFilter?.let {
                binding.serverInstanceDropDown.setText(it.name)
            }
        }

        fun setUpPipelineRecycler() {
            val pipelineRecyclerAdapter = PipelineRecyclerAdapter(
                { editPipeline(it) },
                { launchPipeline(it) }
            )
            binding.insertPipelinesHere.adapter = pipelineRecyclerAdapter
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
                binding.mail = mail
                binding.executePendingBindings()
                l("livePipelineViews.observe ends")
            })
            RecyclerViewCosmetics.makeItAllWork(
                binding.insertPipelinesHere,
                { pipelineRecyclerAdapter.getPipelineList() },
                { deletePipeline(it) },
                requireContext()
            )
            l("setUpPipelineRecycler ends")
        }

        setUpFAB()
        setUpRefreshFAB()
        setUpDropDown()
        setUpPipelineRecycler()
    }

    private fun createPipeline() {
        TODO()
    }

    private fun refreshPipelines() {
        viewModel.refreshButton()
    }

    private fun editPipeline(pipelineView: PipelineView) {
        TODO()
    }

    private fun launchPipeline(pipelineView: PipelineView) {
        TODO()
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
        private const val TAG = "PipelinesFragment"
        private fun l(msg: String) = Log.d(TAG, msg)
        private fun divLog(dashCount: Int = 100) = Log.d(TAG, "-".repeat(dashCount))
    }
}
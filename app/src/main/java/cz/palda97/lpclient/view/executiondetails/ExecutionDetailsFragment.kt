package cz.palda97.lpclient.view.executiondetails

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.R
import cz.palda97.lpclient.SmartMutex
import cz.palda97.lpclient.databinding.FragmentExecutionDetailsBinding
import cz.palda97.lpclient.model.repository.ExecutionDetailRepository
import cz.palda97.lpclient.view.RecyclerViewCosmetics
import cz.palda97.lpclient.viewmodel.executiondetails.ExecutionDetailsViewModel

class ExecutionDetailsFragment : Fragment() {

    companion object {
        private val l = Injector.generateLogFunction(this)
        fun newInstance() = ExecutionDetailsFragment()
    }

    private lateinit var binding: FragmentExecutionDetailsBinding
    private lateinit var viewModel: ExecutionDetailsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_execution_details, container, false)
        val root = binding.root
        viewModel = ExecutionDetailsViewModel.getInstance(this)
        setUpComponents()
        return root
    }

    private val ExecutionDetailRepository.ExecutionDetailRepositoryStatus.errorMessage: String
        get() = when(this) {
            ExecutionDetailRepository.ExecutionDetailRepositoryStatus.OK -> getString(R.string.internal_error)
            ExecutionDetailRepository.ExecutionDetailRepositoryStatus.LOADING -> getString(R.string.internal_error)
            ExecutionDetailRepository.ExecutionDetailRepositoryStatus.INTERNAL_ERROR -> getString(R.string.internal_error)
            ExecutionDetailRepository.ExecutionDetailRepositoryStatus.DOWNLOADING_ERROR -> getString(R.string.error_downloading_component_info)
            ExecutionDetailRepository.ExecutionDetailRepositoryStatus.PARSING_ERROR -> getString(R.string.error_parsing_component_info)
        }

    fun setUpComponents() {

        fun setUpDetail() {
            val adapter = ExecutionDetailRecycleAdapter()
            RecyclerViewCosmetics.attachAdapter(binding.insertComponentsHere, adapter)
            viewModel.liveDetail.observe(viewLifecycleOwner, Observer {
                val detail = it ?: return@Observer
                binding.execution = detail.execution
                val loading = detail.status == ExecutionDetailRepository.ExecutionDetailRepositoryStatus.LOADING
                binding.loading = loading
                if (!loading) {
                    adapter.updateComponentList(detail.components)
                }
                binding.executePendingBindings()
            })
        }

        fun setUpUpdateError() {
            viewModel.liveUpdateError.observe(viewLifecycleOwner, Observer {
                val status = it ?: return@Observer
                if (status == viewModel.updateErrorNeutralValue)
                    return@Observer
                viewModel.resetUpdateError()
                Snackbar.make(binding.root, status.errorMessage, Snackbar.LENGTH_LONG).show()
            })
        }

        setUpDetail()
        setUpUpdateError()
    }

    private val openExecutionInBrowserMutex = SmartMutex()
    private fun openExecutionInBrowser() {
        openExecutionInBrowserMutex.syncScope(lifecycleScope) {
            val url = viewModel.executionLink()
            if (url == null) {
                Snackbar
                    .make(binding.root, R.string.internal_error, Snackbar.LENGTH_SHORT)
                    .show()
                return@syncScope
            }
            try {
                done()
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (_: ActivityNotFoundException) {
                reset()
                Snackbar
                    .make(binding.root, R.string.server_url_is_not_valid, Snackbar.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onResume() {
        openExecutionInBrowserMutex.reset()
        super.onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.execution_detail, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.open_in_browser_item -> {
                openExecutionInBrowser()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
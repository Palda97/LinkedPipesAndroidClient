package cz.palda97.lpclient.view.recentexecution

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.R
import cz.palda97.lpclient.SmartMutex
import cz.palda97.lpclient.databinding.FragmentRecentExecutionBinding
import cz.palda97.lpclient.view.ExecutionDetailActivity
import cz.palda97.lpclient.view.RecyclerViewCosmetics
import cz.palda97.lpclient.view.executions.ExecutionRecycleAdapter
import cz.palda97.lpclient.viewmodel.executions.ExecutionV
import cz.palda97.lpclient.viewmodel.executions.ExecutionsViewModel
import cz.palda97.lpclient.viewmodel.recentexecution.RecentExecutionViewModel

class RecentExecutionFragment : Fragment() {

    companion object {
        private val l = Injector.generateLogFunction(this)
        fun newInstance() = RecentExecutionFragment()
    }

    private lateinit var binding: FragmentRecentExecutionBinding
    private lateinit var viewModel: RecentExecutionViewModel
    private lateinit var executionViewModel: ExecutionsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_recent_execution, container, false)
        val root = binding.root
        viewModel = RecentExecutionViewModel.getInstance(this)
        executionViewModel = ExecutionsViewModel.getInstance(this)
        setUpComponents()
        return root
    }

    fun setUpComponents() {

        fun setUpRecycler(): ExecutionRecycleAdapter {
            val adapter = ExecutionRecycleAdapter(
                { viewExecution(it) },
                {}
            )
            RecyclerViewCosmetics.attachAdapter(binding.insertExecutionsHere, adapter)
            RecyclerViewCosmetics.addDividers(binding.insertExecutionsHere, requireContext())
            viewModel.liveExecution.observe(viewLifecycleOwner, Observer {
                val list = it ?: return@Observer
                adapter.updateExecutionList(list)
            })
            binding.fastscroll.setRecyclerView(binding.insertExecutionsHere)
            return adapter
        }

        fun setUpClearButton(adapter: ExecutionRecycleAdapter) {
            binding.fabClear.setOnClickListener {
                val job = adapter.getList()?.let {
                    binding.fabClear.hide()
                    viewModel.resetRecent(it)
                }
                val act = requireActivity()
                job?.invokeOnCompletion {
                    if (it != null)
                        return@invokeOnCompletion
                    act.finish()
                }
            }
        }

        val adapter = setUpRecycler()
        setUpClearButton(adapter)
    }

    private val smartMutex = SmartMutex()
    private fun viewExecution(execution: ExecutionV) {
        smartMutex.syncScope(lifecycleScope) {
            val isError = !executionViewModel.viewExecution(execution)
            if (isError) {
                Snackbar.make(
                    binding.root,
                    R.string.internal_error,
                    Snackbar.LENGTH_SHORT
                )
                    .show()
                return@syncScope
            }
            ExecutionDetailActivity.start(requireContext())
            done()
        }
    }

    override fun onResume() {
        smartMutex.reset()
        super.onResume()
    }
}
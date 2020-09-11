package cz.palda97.lpclient.view.executions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.FragmentExecutionsBinding
import cz.palda97.lpclient.viewmodel.executions.ExecutionsViewModel

class ExecutionsFragment : Fragment() {

    private lateinit var binding: FragmentExecutionsBinding
    private lateinit var viewModel: ExecutionsViewModel
    private lateinit var refreshFab: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //return inflater.inflate(R.layout.fragment_executions, container, false)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_executions, container, false)
        val root = binding.root
        viewModel = ViewModelProvider(this).get(ExecutionsViewModel::class.java)
        setUpComponents()
        return root
    }

    private fun setUpComponents() {
        fun setUpRefreshFab() {
            refreshFab = binding.fabRefresh.apply {
                setOnClickListener {
                    refreshExecutions()
                }
            }
        }

        setUpRefreshFab()
    }

    private fun refreshExecutions() {
        viewModel.refreshExecutionsButton()
    }
}
package cz.palda97.lpclient.view.executiondetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.FragmentExecutionDetailsBinding
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

    fun setUpComponents() {

        TODO()
    }
}
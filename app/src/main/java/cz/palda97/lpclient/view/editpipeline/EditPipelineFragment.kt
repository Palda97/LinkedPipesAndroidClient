package cz.palda97.lpclient.view.editpipeline

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.FragmentEditPipelineBinding
import cz.palda97.lpclient.view.MainActivity
import cz.palda97.lpclient.viewmodel.editpipeline.EditPipelineViewModel

class EditPipelineFragment : Fragment() {

    companion object {
        private val l = Injector.generateLogFunction(this)
        fun newInstance() =
            EditPipelineFragment()
    }

    private lateinit var viewModel: EditPipelineViewModel
    private lateinit var binding: FragmentEditPipelineBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_pipeline, container, false)
        val root = binding.root
        viewModel = EditPipelineViewModel.getInstance(this)
        setUpComponents()
        MainActivity.switchToFragment = R.id.navigation_pipelines
        return root
    }

    private fun setUpComponents() {
        //
    }
}
package cz.palda97.lpclient.view.editcomponent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.FragmentEditComponentGeneralBinding
import cz.palda97.lpclient.model.entities.pipeline.Component
import cz.palda97.lpclient.viewmodel.editcomponent.EditComponentViewModel
import cz.palda97.lpclient.viewmodel.editpipeline.EditPipelineViewModel
import kotlinx.coroutines.launch

class GeneralFragment : Fragment() {

    private lateinit var binding: FragmentEditComponentGeneralBinding
    private lateinit var viewModel: EditComponentViewModel
    //private lateinit var editPipelineViewModel: EditPipelineViewModel

    //private var currentComponent: Component? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_component_general, container, false)
        val root = binding.root
        viewModel = EditComponentViewModel.getInstance(this)
        //editPipelineViewModel = EditPipelineViewModel.getInstance(this)
        setUpComponents()
        return root
    }

    private fun setUpComponents() {

        fun updateComponent() {
            binding.component?.let {
                viewModel.updateComponent(it)
            }
        }

        fun setUpCurrentComponent() {
            binding.component = viewModel.getComponent()

            binding.prefLabel.doOnTextChanged { _, _, _, _ ->
                updateComponent()
            }
            binding.description.doOnTextChanged { _, _, _, _ ->
                updateComponent()
            }
        }

        fun setUpComponentPersistRepo() {
            lifecycleScope.launch {
                val ready = viewModel.prepareComponent()
                if (!ready) {
                    //TODO()
                    return@launch
                }
                setUpCurrentComponent()
            }
        }

        setUpComponentPersistRepo()
    }

    /*override fun onResume() {
        super.onResume()
        //currentComponent = viewModel.currentComponent
        setUpComponents()
    }*/

    override fun onPause() {
        viewModel.persistComponent()
        super.onPause()
    }

    companion object {
        private val l = Injector.generateLogFunction(this)
        fun getInstance() = GeneralFragment()
    }
}
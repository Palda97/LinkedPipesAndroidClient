package cz.palda97.lpclient.view.editcomponent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.FragmentEditComponentGeneralBinding
import cz.palda97.lpclient.viewmodel.editcomponent.EditComponentViewModel

class GeneralFragment : Fragment() {

    private lateinit var binding: FragmentEditComponentGeneralBinding
    private lateinit var viewModel: EditComponentViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_component_general, container, false)
        val root = binding.root
        viewModel = EditComponentViewModel.getInstance(this)
        setUpComponents()
        return root
    }

    private fun setUpComponents() {
        binding.component = viewModel.currentComponent
    }

    override fun onPause() {
        viewModel.persistComponent()
        super.onPause()
    }

    companion object {
        private val l = Injector.generateLogFunction(this)
        fun getInstance() = GeneralFragment()
    }
}
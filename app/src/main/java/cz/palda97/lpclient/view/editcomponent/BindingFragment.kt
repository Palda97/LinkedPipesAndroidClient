package cz.palda97.lpclient.view.editcomponent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.FragmentEditComponentBindingBinding
import cz.palda97.lpclient.viewmodel.editcomponent.EditComponentViewModel

class BindingFragment : Fragment() {

    private lateinit var binding: FragmentEditComponentBindingBinding
    private lateinit var viewModel: EditComponentViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_component_binding, container, false)
        val root = binding.root
        viewModel = EditComponentViewModel.getInstance(this)
        setUpComponents()
        return root
    }

    private fun setUpComponents() {

        //
    }

    /*override fun onPause() {
        viewModel.persistBinding()
        super.onPause()
    }*/

    companion object {
        private val l = Injector.generateLogFunction(this)
        fun getInstance() = BindingFragment()
    }
}
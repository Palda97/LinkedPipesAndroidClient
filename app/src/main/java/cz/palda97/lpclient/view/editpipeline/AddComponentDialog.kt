package cz.palda97.lpclient.view.editpipeline

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.DialogAddComponentBinding
import cz.palda97.lpclient.model.entities.possiblecomponent.PossibleComponent
import cz.palda97.lpclient.model.repository.PossibleComponentRepository
import cz.palda97.lpclient.model.repository.PossibleComponentRepository.StatusCode.Companion.toStatus
import cz.palda97.lpclient.view.ConfigDropdownMagic.fillWithOptions
import cz.palda97.lpclient.view.SmartArrayAdapter
import cz.palda97.lpclient.viewmodel.editpipeline.AddComponentViewModel

class AddComponentDialog : DialogFragment() {

    private lateinit var binding: DialogAddComponentBinding
    private lateinit var viewModel: AddComponentViewModel

    private var componentAdapter: SmartArrayAdapter<PossibleComponent>? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = DataBindingUtil.inflate(layoutInflater, R.layout.dialog_add_component, null, false)
        viewModel = AddComponentViewModel.getInstance(this)

        val builder = MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setNeutralButton(R.string.cancel) { _, _ ->
                //
            }
            .setPositiveButton(R.string.continue_string) { _, _ ->
                val component = componentAdapter?.lastSelectedItemId
                if (component == null) {
                    Toast.makeText(requireContext(), getString(R.string.no_component_is_selected), Toast.LENGTH_LONG).show()
                } else {
                    viewModel.addComponent(component)
                }
            }

        return builder.create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewModel.liveComponents.observe(viewLifecycleOwner, Observer {
            val statusWithPossibles = it ?: return@Observer
            if (statusWithPossibles.status.result.toStatus != PossibleComponentRepository.StatusCode.OK) {
                //TODO()
                return@Observer
            }
            val options = statusWithPossibles.list.map {
                it to it.prefLabel
            }
            componentAdapter = binding.componentDropDown.fillWithOptions(requireContext(), options) { position, _ ->
                viewModel.lastSelectedComponentPosition = position
            }.apply {
                lastSelectedPosition = viewModel.lastSelectedComponentPosition
            }
        })

        return binding.root
    }

    companion object {
        private val l = Injector.generateLogFunction(this)

        private const val FRAGMENT_TAG = "addComponentDialog"
        fun appear(fragmentManager: FragmentManager) {
            AddComponentDialog().show(fragmentManager, FRAGMENT_TAG)
        }
    }
}
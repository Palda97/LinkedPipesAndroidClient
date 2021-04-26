package cz.palda97.lpclient.view.editcomponent

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
import cz.palda97.lpclient.databinding.DialogAddConnectionBinding
import cz.palda97.lpclient.model.entities.pipeline.Binding
import cz.palda97.lpclient.model.entities.pipeline.Component
import cz.palda97.lpclient.model.repository.ComponentRepository
import cz.palda97.lpclient.model.repository.ComponentRepository.StatusCode.Companion.toStatus
import cz.palda97.lpclient.view.SmartArrayAdapter
import cz.palda97.lpclient.view.ConfigDropdownMagic.fillWithOptions
import cz.palda97.lpclient.viewmodel.editcomponent.EditComponentViewModel

/**
 * Dialog window for creating a new connection.
 */
class CreateConnectionDialog : DialogFragment() {

    private lateinit var binding: DialogAddConnectionBinding
    private lateinit var viewModel: EditComponentViewModel

    private var componentAdapter: SmartArrayAdapter<Component>? = null
    private var bindingAdapter: SmartArrayAdapter<Binding>? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = DataBindingUtil.inflate(layoutInflater, R.layout.dialog_add_connection, null, false)
        viewModel = EditComponentViewModel.getInstance(this)

        val builder = MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setNeutralButton(R.string.cancel) { _, _ ->
                //
            }
            .setPositiveButton(R.string.continue_string) { _, _ ->
                val component = componentAdapter?.lastSelectedItemId
                val binding = bindingAdapter?.lastSelectedItemId
                l(component)
                l(binding)
                if (binding != null && component != null) {
                    viewModel.saveConnection(component, binding)
                } else {
                    Toast.makeText(requireContext(), getString(R.string.select_both_component_and_binding), Toast.LENGTH_LONG).show()
                }
            }

        return builder.create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewModel.connectionComponents.observe(viewLifecycleOwner, Observer {
            val components = it ?: return@Observer
            val options = components.map {
                it to it.prefLabel
            }
            componentAdapter = binding.componentDropDown.fillWithOptions<Component>(requireContext(), options) { position, component ->
                viewModel.lastSelectedComponentPosition = position
                component?.let {
                    viewModel.prepareBindings(it)
                }
            }.apply {
                lastSelectedPosition = viewModel.lastSelectedComponentPosition
            }
        })

        viewModel.connectionBindings.observe(viewLifecycleOwner, Observer {
            val statusWithBinding = it ?: return@Observer
            if (statusWithBinding.status.result.toStatus != ComponentRepository.StatusCode.OK) {
                return@Observer
            }
            val options = statusWithBinding.list.map {
                it to it.prefLabel
            }
            bindingAdapter = binding.bindingDropDown.fillWithOptions<Binding>(requireContext(), options) { position, _ ->
                viewModel.lastSelectedBindingPosition = position
            }.apply {
                lastSelectedPosition = viewModel.lastSelectedBindingPosition
            }
        })

        return binding.root
    }

    companion object {
        private val l = Injector.generateLogFunction(this)

        private const val FRAGMENT_TAG = "createConnectionDialog"

        /**
         * Creates an instance of [CreateConnectionDialog] and shows it.
         */
        fun appear(fragmentManager: FragmentManager) {
            CreateConnectionDialog().show(fragmentManager, FRAGMENT_TAG)
        }
    }
}
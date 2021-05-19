package cz.palda97.lpclient.view.editpipeline

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.DialogChooseServerBinding
import cz.palda97.lpclient.view.ServerDropDownMagic.setUpWithServers
import cz.palda97.lpclient.viewmodel.CommonViewModel
import cz.palda97.lpclient.viewmodel.pipelines.PipelinesViewModel
import cz.palda97.lpclient.viewmodel.settings.SettingsViewModel

/**
 * Dialog window for creating a new pipeline.
 */
class CreatePipelineDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val binding: DialogChooseServerBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.dialog_choose_server, null, false)
        val commonViewModel = CommonViewModel.getInstance(this)
        val settingsViewModel = SettingsViewModel.getInstance(this)
        val pipelineViewModel = PipelinesViewModel.getInstance(this)

        binding.serverInstanceDropDown.setUpWithServers(
            requireContext(),
            settingsViewModel,
            this,
            { commonViewModel.setServerToFilterFun(it) },
            commonViewModel.serverToFilter,
            false
        )

        val builder = MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setNeutralButton(R.string.cancel) { _, _ ->
                //
            }
            .setPositiveButton(R.string.create) { _, _ ->
                val server = commonViewModel.serverToFilter
                if (server == null) {
                    Toast.makeText(requireContext(), R.string.no_server_selected, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                pipelineViewModel.createPipeline(server)
            }

        return builder.create()
    }

    companion object {
        private val l = Injector.generateLogFunction(this)

        private const val FRAGMENT_TAG = "createPipelineDialog"

        /**
         * Creates an instance of [CreatePipelineDialog] and shows it.
         */
        fun appear(fragmentManager: FragmentManager) {
            CreatePipelineDialog().show(fragmentManager, FRAGMENT_TAG)
        }
    }
}
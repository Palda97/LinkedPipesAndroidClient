package cz.palda97.lpclient.view.editcomponent

import android.app.Dialog
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.DialogAddConnectionBinding
import cz.palda97.lpclient.viewmodel.editcomponent.EditComponentViewModel

class CreateConnectionDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val binding: DialogAddConnectionBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.dialog_add_connection, null, false)
        val viewModel = EditComponentViewModel.getInstance(this)

        val builder = MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setNeutralButton(R.string.cancel) { _, _ ->
                //
            }
            /*.setPositiveButton(R.string.continue_string) { _, _ ->
                if (commonViewModel.serverToFilter == null) {
                    Toast.makeText(requireContext(), R.string.no_server_selected, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                EditPipelineActivity.start(requireContext())
            }*/
            .setPositiveButton(R.string.continue_string) { _, _ ->
                //
            }

        return builder.create()
    }

    companion object {
        private val l = Injector.generateLogFunction(this)

        private const val FRAGMENT_TAG = "createConnectionDialog"
        fun appear(fragmentManager: FragmentManager) {
            CreateConnectionDialog().show(fragmentManager, FRAGMENT_TAG)
        }
    }
}
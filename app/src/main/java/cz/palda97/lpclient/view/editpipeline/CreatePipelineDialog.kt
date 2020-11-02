package cz.palda97.lpclient.view.editpipeline

import android.app.Dialog
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.DialogChooseServerBinding

class CreatePipelineDialog: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding: DialogChooseServerBinding = DataBindingUtil.inflate(layoutInflater, R.layout.dialog_choose_server, null, false)
        val builder = MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setTitle(R.string.choose_a_server_instance)
            .setNeutralButton(R.string.cancel) {_, _ ->
                //
            }
            .setPositiveButton(R.string.continue_string) {_, _ ->
                //
            }


        return builder.create()
    }

    companion object {
        private val l = Injector.generateLogFunction(this)

        private const val FRAGMENT_TAG = "createPipelineDialog"
        fun appear(fragmentManager: FragmentManager) {
            CreatePipelineDialog().show(fragmentManager, FRAGMENT_TAG)
        }
    }
}
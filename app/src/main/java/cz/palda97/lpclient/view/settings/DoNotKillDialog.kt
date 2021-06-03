package cz.palda97.lpclient.view.settings

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.method.LinkMovementMethod
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.DialogDoNotKillBinding
import cz.palda97.lpclient.viewmodel.settings.SettingsViewModel

/**
 * Dialog window for informing user about Android manufacturers killing applications.
 */
class DoNotKillDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val binding: DialogDoNotKillBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.dialog_do_not_kill, null, false)
        binding.doNotKillInfoTextview.movementMethod = LinkMovementMethod.getInstance()
        val viewModel = SettingsViewModel.getInstance(this)

        val builder = MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setNeutralButton(R.string.stop_reminding) { _, _ ->
                viewModel.stopRemindingDoNotKill = true
            }
            .setNegativeButton(R.string.open_settings) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", requireContext().packageName, null)
                }
                startActivity(intent)
            }
            .setPositiveButton(R.string.ok) { _, _ ->
                //
            }

        return builder.create()
    }

    companion object {
        private val l = Injector.generateLogFunction(this)

        private const val FRAGMENT_TAG = "doNotKillDialog"

        /**
         * Creates an instance of [DoNotKillDialog] and shows it.
         */
        fun appear(fragmentManager: FragmentManager) {
            DoNotKillDialog().show(fragmentManager, FRAGMENT_TAG)
        }
    }
}
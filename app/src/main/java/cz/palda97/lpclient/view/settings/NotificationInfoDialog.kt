package cz.palda97.lpclient.view.settings

import android.app.Dialog
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.DialogNotificationInfoBinding
import cz.palda97.lpclient.viewmodel.settings.SettingsViewModel

/**
 * Dialog window for informing user about notification refresh rate.
 */
class NotificationInfoDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val binding: DialogNotificationInfoBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.dialog_notification_info, null, false)
        val viewModel = SettingsViewModel.getInstance(this)
        binding.automaticRefreshRate = viewModel.automaticRefreshRate

        val builder = MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setNeutralButton(R.string.stop_reminding) { _, _ ->
                viewModel.stopRemindingMonitorBackground = true
            }
            .setPositiveButton(R.string.ok) { _, _ ->
                //
            }

        return builder.create()
    }

    companion object {
        private val l = Injector.generateLogFunction(this)

        private const val FRAGMENT_TAG = "notificationInfoDialog"

        /**
         * Creates an instance of [NotificationInfoDialog] and shows it.
         */
        fun appear(fragmentManager: FragmentManager) {
            NotificationInfoDialog().show(fragmentManager, FRAGMENT_TAG)
        }
    }
}
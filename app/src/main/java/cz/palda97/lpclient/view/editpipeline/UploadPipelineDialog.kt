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
import cz.palda97.lpclient.databinding.DialogUploadPipelineBinding
import cz.palda97.lpclient.viewmodel.editpipeline.EditPipelineViewModel

class UploadPipelineDialog : DialogFragment() {

    private lateinit var binding: DialogUploadPipelineBinding
    private lateinit var viewModel: EditPipelineViewModel

    //private var componentAdapter: SmartArrayAdapter<PossibleComponent>? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = DataBindingUtil.inflate(layoutInflater, R.layout.dialog_upload_pipeline, null, false)
        viewModel = EditPipelineViewModel.getInstance(this)

        val builder = MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setNeutralButton(R.string.cancel) { _, _ ->
                //
            }
            .setPositiveButton(R.string.continue_string) { _, _ ->
                viewModel.uploadPipeline()
            }

        return builder.create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding.pipelineView = viewModel.currentPipelineView

        return binding.root
    }

    companion object {
        private val l = Injector.generateLogFunction(this)

        private const val FRAGMENT_TAG = "uploadPipelineDialog"
        fun appear(fragmentManager: FragmentManager) {
            UploadPipelineDialog().show(fragmentManager, FRAGMENT_TAG)
        }
    }
}
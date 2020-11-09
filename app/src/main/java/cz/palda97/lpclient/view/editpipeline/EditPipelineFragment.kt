package cz.palda97.lpclient.view.editpipeline

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.DynamicButtonBinding
import cz.palda97.lpclient.databinding.FragmentEditPipelineBinding
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.entities.pipeline.Pipeline
import cz.palda97.lpclient.model.repository.PipelineRepository
import cz.palda97.lpclient.model.repository.PipelineRepository.CacheStatus.Companion.toStatus
import cz.palda97.lpclient.view.MainActivity
import cz.palda97.lpclient.view.editpipeline.CoordinateConverter.resize
import cz.palda97.lpclient.viewmodel.editpipeline.EditPipelineViewModel
import io.github.hyuwah.draggableviewlib.DraggableListener
import io.github.hyuwah.draggableviewlib.makeDraggable
import kotlinx.coroutines.*
import kotlin.math.roundToInt

class EditPipelineFragment : Fragment() {

    companion object {
        private val l = Injector.generateLogFunction(this)
        fun newInstance() =
            EditPipelineFragment()
    }

    private lateinit var viewModel: EditPipelineViewModel
    private lateinit var binding: FragmentEditPipelineBinding

    private var currentPipeline: Pipeline? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_pipeline, container, false)
        val root = binding.root
        viewModel = EditPipelineViewModel.getInstance(this)
        setUpPipeline()
        setUpComponents()
        MainActivity.switchToFragment = R.id.navigation_pipelines
        return root
    }

    private fun setUpPipeline() {
        viewModel.currentPipeline.observe(viewLifecycleOwner, Observer {
            val mail = it ?: return@Observer
            when(mail.status) {
                MailPackage.Status.LOADING -> {
                    //
                }
                MailPackage.Status.ERROR -> {
                    val text: String = when(mail.msg.toStatus) {
                        PipelineRepository.CacheStatus.SERVER_NOT_FOUND -> "The server instance is no longer registered here!"
                        PipelineRepository.CacheStatus.DOWNLOAD_ERROR -> "Error while downloading the pipeline!"
                        PipelineRepository.CacheStatus.PARSING_ERROR -> "Pipeline could not be parsed!"
                        PipelineRepository.CacheStatus.NO_PIPELINE_TO_LOAD -> "Error while loading pipeline!"
                        PipelineRepository.CacheStatus.INTERNAL_ERROR -> "Internal error."
                    }
                    Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG)
                        .setAction(null.toString(), null)
                        .setAnchorView(binding.editPipelineBottomButtons)
                        .show()
                }
                MailPackage.Status.OK -> {
                    mail.mailContent!!
                    currentPipeline = mail.mailContent
                    l("Pipeline is OK")
                    l(currentPipeline.toString())
                    displayPipeline()
                }
            }
            binding.mail = mail
            binding.executePendingBindings()
        })
    }

    override fun onPause() {
        currentPipeline?.let {
            viewModel.savePipeline(it)
        }
        super.onPause()
    }

    private fun setUpComponents() {
        //
    }

    private fun displayPipeline() {
        binding.frameLayout.removeAllViews()

        binding.frameLayout.resize(currentPipeline!!.components)

        currentPipeline!!.components.forEach {
            val buttonBinding: DynamicButtonBinding = DataBindingUtil.inflate(layoutInflater, R.layout.dynamic_button, null, false)
            buttonBinding.button.makeDraggable(
                draggableListener = object : DraggableListener {
                    override fun onPositionChanged(view: View) {
                        binding.scrollView.requestDisallowInterceptTouchEvent(true)
                        binding.horizontalScrollView.requestDisallowInterceptTouchEvent(true)
                        val (x, y) = CoordinateConverter.fromDisplay(view.x, view.y)
                        it.x = x
                        it.y = y
                    }
                }
            )
            with(buttonBinding.button) {
                val (x, y) = CoordinateConverter.toDisplay(it.x, it.y)
                this.x = x
                this.y = y
                text = it.prefLabel
            }
            binding.frameLayout.addView(buttonBinding.root)
        }

        CoordinateConverter.coordsToScrollTo(currentPipeline!!.components)?.let {
            val (x, y) = it
            lifecycleScope.launch {
                delay(50L)
                binding.horizontalScrollView.scrollX = x
                binding.scrollView.scrollY = y
            }
        }
    }
}
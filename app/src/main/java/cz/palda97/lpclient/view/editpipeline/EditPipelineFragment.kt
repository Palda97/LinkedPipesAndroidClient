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
        private const val XSCALE: Float = 2.toFloat()
        private const val YSCALE: Float = 1.5.toFloat()
        private const val XOFFSET = 1000
        private const val YOFFSET = 1000
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

        currentPipeline!!.components.maxBy {
            it.x
        }?.let {
            val params = binding.frameLayout.layoutParams ?: return@let
            params.width = (it.x * XSCALE).roundToInt() + XOFFSET
            binding.frameLayout.layoutParams = params
        }
        currentPipeline!!.components.maxBy {
            it.y
        }?.let {
            val params = binding.frameLayout.layoutParams ?: return@let
            params.height = (it.y * YSCALE).roundToInt() + YOFFSET
            binding.frameLayout.layoutParams = params
        }

        currentPipeline!!.components.forEach {
            val buttonBinding: DynamicButtonBinding = DataBindingUtil.inflate(layoutInflater, R.layout.dynamic_button, null, false)
            buttonBinding.button.makeDraggable(
                draggableListener = object : DraggableListener {
                    override fun onPositionChanged(view: View) {
                        binding.scrollView.requestDisallowInterceptTouchEvent(true)
                        binding.horizontalScrollView.requestDisallowInterceptTouchEvent(true)
                        it.x = (view.x / XSCALE).toInt()
                        it.y = (view.y / YSCALE).toInt()
                    }
                }
            )
            with(buttonBinding.button) {
                x = it.x.toFloat() * XSCALE
                y = it.y.toFloat() * YSCALE
                text = it.prefLabel
            }
            binding.frameLayout.addView(buttonBinding.root)
        }

        val minX = currentPipeline!!.components.minBy {
                it.x
            }?.x
        val minY = currentPipeline!!.components.minBy {
            it.y
        }?.y

        if (minX != null && minY != null) {
            lifecycleScope.launch {
                delay(50L)
                binding.horizontalScrollView.scrollX = (minX * XSCALE).roundToInt()
                binding.scrollView.scrollY = (minY * YSCALE).roundToInt()
            }
        }
    }
}
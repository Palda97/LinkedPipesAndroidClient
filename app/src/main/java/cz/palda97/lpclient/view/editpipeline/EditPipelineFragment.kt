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
import cz.palda97.lpclient.databinding.DynamicImageviewBinding
import cz.palda97.lpclient.databinding.FragmentEditPipelineBinding
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.entities.pipeline.Component
import cz.palda97.lpclient.model.entities.pipeline.Pipeline
import cz.palda97.lpclient.model.entities.pipeline.Vertex
import cz.palda97.lpclient.model.repository.PipelineRepository
import cz.palda97.lpclient.model.repository.PipelineRepository.CacheStatus.Companion.toStatus
import cz.palda97.lpclient.view.MainActivity
import cz.palda97.lpclient.view.editpipeline.CoordinateConverter.resize
import cz.palda97.lpclient.viewmodel.editpipeline.EditPipelineViewModel
import io.github.hyuwah.draggableviewlib.DraggableListener
import io.github.hyuwah.draggableviewlib.makeDraggable
import kotlinx.coroutines.*

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
                        PipelineRepository.CacheStatus.SERVER_NOT_FOUND -> getString(R.string.server_instance_no_longer_registered)
                        PipelineRepository.CacheStatus.DOWNLOAD_ERROR -> getString(R.string.error_while_downloading_pipeline)
                        PipelineRepository.CacheStatus.PARSING_ERROR -> getString(R.string.pipeline_could_not_be_parsed)
                        PipelineRepository.CacheStatus.NO_PIPELINE_TO_LOAD -> getString(R.string.error_while_loading_pipeline)
                        PipelineRepository.CacheStatus.INTERNAL_ERROR -> getString(R.string.internal_error)
                    }
                    Snackbar.make(binding.root, text, Snackbar.LENGTH_INDEFINITE)
                        .setAction(getString(R.string.try_again)) {
                            tryAgain()
                        }
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

    private fun tryAgain() {
        viewModel.retryCachePipeline()
    }

    override fun onPause() {
        currentPipeline?.let {
            viewModel.savePipeline(it)
        }
        super.onPause()
    }

    private fun setUpComponents() {

        fun setUpFAB() {
            binding.fab.setOnClickListener {
                l("click")
            }
        }

        fun setUpCancelButton() {
            binding.cancelButton.setOnClickListener {
                requireActivity().finish()
            }
        }

        setUpCancelButton()
        setUpFAB()
    }

    private fun disableScrollViewsForAWhile() {
        binding.scrollView.requestDisallowInterceptTouchEvent(true)
        binding.horizontalScrollView.requestDisallowInterceptTouchEvent(true)
    }

    private val density: Float? by lazy {
        resources.displayMetrics?.density
    }

    private fun displayComponents() {
        val buttonMap: MutableMap<Component, View> = HashMap()
        currentPipeline!!.components.forEach {
            val buttonBinding: DynamicButtonBinding = DataBindingUtil.inflate(layoutInflater, R.layout.dynamic_button, null, false)
            buttonBinding.button.makeDraggable(
                draggableListener = object : DraggableListener {
                    override fun onPositionChanged(view: View) {
                        disableScrollViewsForAWhile()
                        val (x, y) = CoordinateConverter.fromDisplay(view.x, view.y, density)
                        it.x = x
                        it.y = y
                        binding.pipelineLayout.componentsAndButtons?.let { map ->
                            map[it] = view
                            binding.pipelineLayout.invalidate()
                        }
                    }
                }
            )
            with(buttonBinding.button) {
                val (x, y) = CoordinateConverter.toDisplay(it.x, it.y, density)
                this.x = x
                this.y = y
                text = it.prefLabel
            }
            binding.pipelineLayout.addView(buttonBinding.root)
            buttonMap[it] = buttonBinding.button
        }
        binding.pipelineLayout.componentsAndButtons = buttonMap
    }

    private fun scrollToComponents() {
        CoordinateConverter.coordsToScrollTo(currentPipeline!!.components, density)?.let {
            val (x, y) = it
            lifecycleScope.launch {
                delay(50L)
                binding.horizontalScrollView.scrollX = x
                binding.scrollView.scrollY = y
            }
        }
    }

    private fun displayVertexes() {
        val vertexMap: MutableMap<Vertex, View> = HashMap()
        currentPipeline!!.vertexes.forEach {
            val vertexBinding: DynamicImageviewBinding = DataBindingUtil.inflate(layoutInflater, R.layout.dynamic_imageview, null, false)
            vertexBinding.imageView.makeDraggable(
                draggableListener = object : DraggableListener {
                    override fun onPositionChanged(view: View) {
                        disableScrollViewsForAWhile()
                        val (x, y) = CoordinateConverter.fromDisplay(view.x, view.y, density)
                        it.x = x
                        it.y = y
                        binding.pipelineLayout.vertexesAndButtons?.let { map ->
                            map[it] = view
                            binding.pipelineLayout.invalidate()
                        }
                    }
                }
            )
            with(vertexBinding.imageView) {
                val (x, y) = CoordinateConverter.toDisplay(it.x, it.y, density)
                this.x = x
                this.y = y
            }
            binding.pipelineLayout.addView(vertexBinding.root)
            vertexMap[it] = vertexBinding.imageView
        }
        binding.pipelineLayout.vertexesAndButtons = vertexMap
    }

    private fun displayPipeline() {
        binding.pipelineLayout.removeAllViews()
        binding.pipelineLayout.resize(currentPipeline!!.components)
        displayComponents()
        if (viewModel.shouldScroll)
            scrollToComponents()
        displayVertexes()
        binding.pipelineLayout.currentPipeline = currentPipeline
    }
}
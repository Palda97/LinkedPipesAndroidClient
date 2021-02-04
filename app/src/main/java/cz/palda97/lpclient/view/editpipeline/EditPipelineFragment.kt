package cz.palda97.lpclient.view.editpipeline

import android.os.Bundle
import android.view.*
import android.widget.Toast
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
import cz.palda97.lpclient.model.repository.PossibleComponentRepository
import cz.palda97.lpclient.view.EditComponentActivity
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

    private fun savePipeline() {
        currentPipeline?.let {
            viewModel.savePipeline(it)
        }
    }

    override fun onPause() {
        savePipeline()
        super.onPause()
    }

    private fun getCoords(): Pair<Int, Int> {
        val x = binding.horizontalScrollView.scrollX
        val y = binding.scrollView.scrollY
        val shift = resources.displayMetrics?.let {
            val denominator = 3.toDouble()
            val dimensions = CoordinateConverter.fromDisplay(it.widthPixels.toFloat(), it.heightPixels.toFloat(), it.density)
            (dimensions.first / denominator).roundToInt() to (dimensions.second / denominator).roundToInt()
        } ?: 0 to 0
        val leftTopCorner = CoordinateConverter.fromDisplay(x.toFloat(), y.toFloat(), density)
        return leftTopCorner.first + shift.first to leftTopCorner.second + shift.second
    }

    private val PossibleComponentRepository.StatusCode.errorMessage: String
        get() = when(this) {
            PossibleComponentRepository.StatusCode.NO_CONNECT -> getString(R.string.can_not_connect_to_server)
            PossibleComponentRepository.StatusCode.INTERNAL_ERROR -> getString(R.string.internal_error)
            PossibleComponentRepository.StatusCode.DOWNLOADING_ERROR -> getString(R.string.error_while_downloading_default_configuration)
            PossibleComponentRepository.StatusCode.PARSING_ERROR -> getString(R.string.error_while_parsing_default_configuration)
            PossibleComponentRepository.StatusCode.OK -> getString(R.string.internal_error)
            PossibleComponentRepository.StatusCode.DOWNLOAD_IN_PROGRESS -> getString(R.string.internal_error)
            PossibleComponentRepository.StatusCode.SERVER_NOT_FOUND ->  getString(R.string.server_instance_no_longer_registered)
        }

    private val PipelineRepository.StatusCode.message: String
        get() = when(this) {
            PipelineRepository.StatusCode.NO_CONNECT -> getString(R.string.can_not_connect_to_server)
            PipelineRepository.StatusCode.INTERNAL_ERROR -> getString(R.string.internal_error)
            PipelineRepository.StatusCode.NEUTRAL -> getString(R.string.internal_error)
            PipelineRepository.StatusCode.UPLOADING_ERROR -> getString(R.string.error_while_uploading_pipeline)
            PipelineRepository.StatusCode.PARSING_ERROR -> getString(R.string.internal_error)
            PipelineRepository.StatusCode.OK -> getString(R.string.pipeline_has_been_saved)
            PipelineRepository.StatusCode.UPLOAD_IN_PROGRESS -> getString(R.string.internal_error)
        }

    private fun uploadPipeline() {
        val job = viewModel.uploadPipelineButton(currentPipeline) ?: return
        job.invokeOnCompletion {
            if (it != null) {
                return@invokeOnCompletion
            }
            UploadPipelineDialog.appear(parentFragmentManager)
        }
    }

    private fun setUpComponents() {

        fun setUpFAB() {
            binding.fab.setOnClickListener {
                viewModel.addComponent(getCoords())
                savePipeline()
                AddComponentDialog.appear(parentFragmentManager)
            }
        }

        fun setUpCancelButton() {
            binding.cancelButton.setOnClickListener {
                requireActivity().finish()
            }
        }

        fun setUpSaveButton() {
            binding.saveButton.setOnClickListener {
                uploadPipeline()
            }
            viewModel.liveUploadStatus.observe(viewLifecycleOwner, Observer {
                val status = it ?: return@Observer
                if (status == PipelineRepository.StatusCode.NEUTRAL)
                    return@Observer
                viewModel.resetUploadStatus()
                binding.uploading = status == PipelineRepository.StatusCode.UPLOAD_IN_PROGRESS
                binding.executePendingBindings()
                val text = when(status) {
                    PipelineRepository.StatusCode.UPLOAD_IN_PROGRESS -> {
                        return@Observer
                    }
                    else -> status.message
                }
                Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG)
                    .setAnchorView(binding.editPipelineBottomButtons)
                    .show()
            })
        }

        fun setUpAddComponentErrors() {
            viewModel.liveAddComponentStatus.observe(viewLifecycleOwner, Observer {
                val status = it ?: return@Observer
                if (status == PossibleComponentRepository.StatusCode.OK)
                    return@Observer
                viewModel.resetAddComponentStatus()
                Snackbar
                    .make(binding.root, status.errorMessage, Snackbar.LENGTH_LONG)
                    .setAnchorView(binding.editPipelineBottomButtons)
                    .show()
            })
        }

        setUpCancelButton()
        setUpFAB()
        setUpSaveButton()
        setUpAddComponentErrors()
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
            buttonBinding.button.setOnClickListener {_ ->
                editComponent(it)
            }
        }
        binding.pipelineLayout.componentsAndButtons = buttonMap
    }

    private fun editComponent(component: Component) {
        val pipeline = currentPipeline ?: return Unit.also {
            Toast.makeText(requireContext(), R.string.internal_error, Toast.LENGTH_SHORT).show()
        }
        viewModel.editComponent(component, pipeline.templates)
        EditComponentActivity.start(requireContext())
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
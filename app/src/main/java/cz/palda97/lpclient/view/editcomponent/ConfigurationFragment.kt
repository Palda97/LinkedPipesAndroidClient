package cz.palda97.lpclient.view.editcomponent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.ConfigInputBinding
import cz.palda97.lpclient.databinding.FragmentEditComponentConfigurationBinding
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.StatusPackage
import cz.palda97.lpclient.model.entities.pipeline.Component
import cz.palda97.lpclient.model.entities.pipeline.ConfigInput
import cz.palda97.lpclient.model.entities.pipeline.DialogJs
import cz.palda97.lpclient.model.entities.pipeline.Pipeline
import cz.palda97.lpclient.model.repository.ComponentRepository
import cz.palda97.lpclient.viewmodel.editcomponent.EditComponentViewModel
import cz.palda97.lpclient.viewmodel.editpipeline.EditPipelineViewModel

class ConfigurationFragment : Fragment() {

    private lateinit var binding: FragmentEditComponentConfigurationBinding
    private lateinit var viewModel: EditComponentViewModel
    private lateinit var editPipelineViewModel: EditPipelineViewModel

    private var currentPipeline: Pipeline? = null
    private var currentComponent: Component? = null
    private var configBindings: List<ConfigInputBinding>? = null
    private var currentDialogJs: DialogJs? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_component_configuration, container, false)
        val root = binding.root
        viewModel = EditComponentViewModel.getInstance(this)
        editPipelineViewModel = EditPipelineViewModel.getInstance(this)
        setUpComponents()
        return root
    }

    private val ComponentRepository.StatusCode.errorMessage: String
        get() = when(this) {
            ComponentRepository.StatusCode.NO_CONNECT -> getString(R.string.can_not_connect_to_server)
            ComponentRepository.StatusCode.INTERNAL_ERROR -> getString(R.string.internal_error)
            ComponentRepository.StatusCode.SERVER_NOT_FOUND -> getString(R.string.server_instance_no_longer_registered)
            ComponentRepository.StatusCode.DOWNLOADING_ERROR -> getString(R.string.error_while_downloading_component_configuration)
            ComponentRepository.StatusCode.PARSING_ERROR -> getString(R.string.error_while_parsing_configuration)
            ComponentRepository.StatusCode.OK -> getString(R.string.internal_error)
        }

    private val loadingMediator = object {
        private var configInput: StatusPackage = MailPackage.loading()
        private var jsMap: StatusPackage = MailPackage.loading()
        fun updateConfigInput(status: StatusPackage): StatusPackage {
            configInput = status
            return check()
        }
        fun updateDialogJs(status: StatusPackage): StatusPackage {
            jsMap = status
            return check()
        }
        private fun check(): StatusPackage {
            l("check: configInput = ${configInput.status}; jsMap = ${jsMap.status}")
            if (configInput.isError || jsMap.isError)
                return MailPackage.error("${configInput.msg}${jsMap.msg}")
            if (configInput.isLoading || jsMap.isLoading)
                return MailPackage.loading()
            return MailPackage.ok()
        }
    }

    private fun showErrorSnackbar(text: String) {
        Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG)
            .show()
    }

    private fun setUpComponents() {

        fun setUpConfigInputs() {
            viewModel.liveConfigInput.observe(viewLifecycleOwner, Observer {
                val mail = it ?: return@Observer
                if (mail.isLoading) {
                    binding.mail = loadingMediator.updateConfigInput(MailPackage.loading())
                    return@Observer
                }
                if (!mail.isOk) {
                    l("mail is not ok")
                    return@Observer
                }
                val text: String = when(val res = mail.mailContent!!) {
                    is Either.Left -> res.value.errorMessage
                    is Either.Right -> {
                        displayConfigInput(res.value)
                        fillConfigInput()
                        binding.mail = loadingMediator.updateConfigInput(MailPackage.ok())
                        ""
                    }
                }
                if (text.isNotEmpty()) {
                    binding.mail = loadingMediator.updateConfigInput(MailPackage.error())
                    showErrorSnackbar(text)
                }
            })
        }

        fun setUpJsMap() {
            viewModel.liveJsMap.observe(viewLifecycleOwner, Observer {
                val mail = it ?: return@Observer
                if (mail.isLoading) {
                    binding.mail = loadingMediator.updateDialogJs(MailPackage.loading())
                    return@Observer
                }
                if (!mail.isOk) {
                    l("mail is not ok")
                    return@Observer
                }
                val text: String = when(val res = mail.mailContent!!) {
                    is Either.Left -> res.value.errorMessage
                    is Either.Right -> {
                        currentDialogJs = res.value
                        fillConfigInput()
                        binding.mail = loadingMediator.updateDialogJs(MailPackage.ok())
                        ""
                    }
                }
                if (text.isNotEmpty()) {
                    binding.mail = loadingMediator.updateDialogJs(MailPackage.error())
                    showErrorSnackbar(text)
                }
            })
        }

        fun setUpPipeline() {
            editPipelineViewModel.currentPipeline.observe(viewLifecycleOwner, Observer {
                val mail = it ?: return@Observer
                if (!mail.isOk) {
                    return@Observer
                }
                currentPipeline = mail.mailContent
                fillConfigInput()
            })
        }

        setUpConfigInputs()
        setUpJsMap()
        setUpPipeline()
    }

    private fun displayConfigInput(configInputList: List<ConfigInput>) {
        configBindings = configInputList.map {
            val configInputBinding: ConfigInputBinding = DataBindingUtil.inflate(layoutInflater, R.layout.config_input, null, false)
            configInputBinding.configInput = it
            configInputBinding.executePendingBindings()
            binding.insertConfigInputsHere.addView(configInputBinding.root)
            configInputBinding
        }
    }

    private fun fillConfigInput() {
        //l("fillConfigInput ${currentPipeline != null}, ${configBindings != null}, ${currentComponent != null}")
        val pipeline = currentPipeline ?: return
        val cBindings = configBindings ?: return
        val component = currentComponent ?: return
        val dialogJs = currentDialogJs ?: return
        l("fillConfigInput")
        val configuration = pipeline.configurations.find {
            it.id == component.configurationId
        } ?: return Unit.also { l("configuration was not found") }

        cBindings.forEach {
            val configInput = it.configInput!!
            val translated = dialogJs.getFullPropertyName(configInput.id) ?: configInput.id
            val string = configuration.getString(translated) ?: ""
            when(configInput.type) {
                ConfigInput.Type.EDIT_TEXT -> it.editText.editText!!.setText(string)
                ConfigInput.Type.SWITCH -> it.switchMaterial.isChecked = string.toBoolean()
                ConfigInput.Type.DROPDOWN -> {
                    it.dropdown.fillWithOptions(configInput.options)
                }
                ConfigInput.Type.TEXT_AREA -> {
                    it.textArea.setText(string)
                }
            }
        }
    }

    private fun MaterialAutoCompleteTextView.fillWithOptions(options: List<Pair<String, String>>) {
        class PairWrapper(val pair: Pair<String, String>) {
            override fun toString() = pair.second
        }
        val adapter = ArrayAdapter<PairWrapper>(requireContext(), R.layout.dropdown_item_text_view)
        val list = options.map {
            PairWrapper(it)
        }
        adapter.addAll(list)
        adapter.notifyDataSetChanged()
        setAdapter(adapter)
    }

    override fun onResume() {
        super.onResume()
        currentComponent = viewModel.currentComponent
        fillConfigInput()
    }

    companion object {
        private val l = Injector.generateLogFunction(this)
        fun getInstance() = ConfigurationFragment()
    }
}
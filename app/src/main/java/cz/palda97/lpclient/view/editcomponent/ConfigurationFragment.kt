package cz.palda97.lpclient.view.editcomponent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.FragmentEditComponentConfigurationBinding
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.entities.pipeline.ConfigInput
import cz.palda97.lpclient.model.repository.ComponentRepository
import cz.palda97.lpclient.viewmodel.editcomponent.EditComponentViewModel

class ConfigurationFragment : Fragment() {

    private lateinit var binding: FragmentEditComponentConfigurationBinding
    private lateinit var viewModel: EditComponentViewModel

    //private var mutableComponent: MutableComponent? = null
    private var configInputList: List<ConfigInput>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_component_configuration, container, false)
        val root = binding.root
        viewModel = EditComponentViewModel.getInstance(this)
        setUpComponents()
        return root
    }

    private fun setUpComponents() {

        fun setUpConfigInputs() {
            viewModel.liveConfigInput.observe(viewLifecycleOwner, Observer {
                val mail = it ?: return@Observer
                if (mail.isLoading) {
                    binding.mail = MailPackage.loading()
                    return@Observer
                }
                if (!mail.isOk) {
                    l("mail is not ok")
                    return@Observer
                }
                val text: String = when(val res = mail.mailContent!!) {
                    is Either.Left -> when(res.value) {
                        ComponentRepository.StatusCode.NO_CONNECT -> getString(R.string.can_not_connect_to_server)
                        ComponentRepository.StatusCode.INTERNAL_ERROR -> getString(R.string.internal_error)
                        ComponentRepository.StatusCode.SERVER_NOT_FOUND -> getString(R.string.server_instance_no_longer_registered)
                        ComponentRepository.StatusCode.DOWNLOADING_ERROR -> getString(R.string.error_while_downloading_component_configuration)
                        ComponentRepository.StatusCode.PARSING_ERROR -> getString(R.string.error_while_parsing_configuration)
                        ComponentRepository.StatusCode.OK -> getString(R.string.internal_error)
                    }
                    is Either.Right -> {
                        configInputList = res.value
                        displayConfigInput()
                        binding.mail = MailPackage.ok()
                        ""
                    }
                }
                if (text.isNotEmpty()) {
                    binding.mail = MailPackage.error()
                    Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG)
                        .show()
                }
            })
        }

        setUpConfigInputs()
    }

    private fun displayConfigInput() {
        l("----------------------------------------------------------------------------------------------------")
        val configInput = configInputList ?: return Unit.also {
            l("null")
        }
        configInput.forEach {
            l(it)
        }
        l("----------------------------------------------------------------------------------------------------")
    }

    companion object {
        private val l = Injector.generateLogFunction(this)
        fun getInstance() = ConfigurationFragment()
    }
}
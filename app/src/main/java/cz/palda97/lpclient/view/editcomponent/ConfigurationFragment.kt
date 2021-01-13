package cz.palda97.lpclient.view.editcomponent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.FragmentEditComponentConfigurationBinding
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.StatusPackage
import cz.palda97.lpclient.model.entities.pipeline.*
import cz.palda97.lpclient.model.repository.ComponentRepository
import cz.palda97.lpclient.model.repository.ComponentRepository.StatusCode.Companion.toStatus
import cz.palda97.lpclient.viewmodel.editcomponent.ConfigInputComplete
import cz.palda97.lpclient.viewmodel.editcomponent.EditComponentViewModel
import cz.palda97.lpclient.viewmodel.editcomponent.OnlyStatus
import kotlinx.coroutines.*

class ConfigurationFragment : Fragment() {

    private lateinit var binding: FragmentEditComponentConfigurationBinding
    private lateinit var viewModel: EditComponentViewModel

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

    private val ComponentRepository.StatusCode.errorMessage: String
        get() = when(this) {
            ComponentRepository.StatusCode.NO_CONNECT -> getString(R.string.can_not_connect_to_server)
            ComponentRepository.StatusCode.INTERNAL_ERROR -> getString(R.string.internal_error)
            ComponentRepository.StatusCode.SERVER_NOT_FOUND -> getString(R.string.server_instance_no_longer_registered)
            ComponentRepository.StatusCode.DOWNLOADING_ERROR -> getString(R.string.error_while_downloading_component_configuration)
            ComponentRepository.StatusCode.PARSING_ERROR -> getString(R.string.error_while_parsing_configuration)
            ComponentRepository.StatusCode.OK -> getString(R.string.internal_error)
            ComponentRepository.StatusCode.DOWNLOAD_IN_PROGRESS -> getString(R.string.internal_error)
        }

    /*private val loadingMediator = object {
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
    }*/

    /*private fun showErrorSnackbar(text: String) {
        Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG)
            .show()
    }*/

    private fun setUpComponents() {

        fun setUpConfigInputRecycler() {
            val adapter = ConfigInputAdapter(
                requireContext(),
                { viewModel.configGetString(it) },
                { key, value -> viewModel.configSetString(key, value) }
            )
            binding.insertConfigInputsHere.adapter = adapter
            viewModel.liveConfigInputContext.observe(viewLifecycleOwner, Observer {
                val configContext = it ?: return@Observer
                val text = when(configContext) {
                    is OnlyStatus -> configContext.status.errorMessage
                    is ConfigInputComplete -> {
                        adapter.updateConfigInputList(configContext)
                        binding.noInstances = configContext.configInputs.isEmpty()
                        ""
                    }
                }
                binding.mail = when(configContext.status) {
                    ComponentRepository.StatusCode.OK -> MailPackage.ok()
                    ComponentRepository.StatusCode.DOWNLOAD_IN_PROGRESS -> MailPackage.loading()
                    else -> MailPackage.error(text)
                }
                binding.executePendingBindings()
            })
            //binding.fastscroll.setRecyclerView(binding.insertConfigInputsHere)
        }

        setUpConfigInputRecycler()
    }

    override fun onPause() {
        viewModel.persistConfiguration()
        super.onPause()
    }

    companion object {
        private val l = Injector.generateLogFunction(this)
        fun getInstance() = ConfigurationFragment()
    }
}
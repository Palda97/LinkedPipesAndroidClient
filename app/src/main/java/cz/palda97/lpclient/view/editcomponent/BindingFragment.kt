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
import cz.palda97.lpclient.databinding.FragmentEditComponentBindingBinding
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.entities.pipeline.Binding
import cz.palda97.lpclient.model.entities.pipeline.Connection
import cz.palda97.lpclient.model.repository.ComponentRepository
import cz.palda97.lpclient.model.repository.ComponentRepository.StatusCode.Companion.toStatus
import cz.palda97.lpclient.view.RecyclerViewCosmetics
import cz.palda97.lpclient.viewmodel.editcomponent.*

class BindingFragment : Fragment() {

    private lateinit var binding: FragmentEditComponentBindingBinding
    private lateinit var viewModel: EditComponentViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_component_binding, container, false)
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
            ComponentRepository.StatusCode.DOWNLOADING_ERROR -> getString(R.string.error_while_downloading_component_bindings)
            ComponentRepository.StatusCode.PARSING_ERROR -> getString(R.string.error_while_parsing_bindings)
            ComponentRepository.StatusCode.OK -> getString(R.string.internal_error)
            ComponentRepository.StatusCode.DOWNLOAD_IN_PROGRESS -> getString(R.string.internal_error)
        }

    private fun setUpComponents() {

        fun setUpButtons() {
            val adapter = BindingAdapter(::addConnection)
            binding.insertBindingsHere.adapter = adapter
            viewModel.liveBinding.observe(viewLifecycleOwner, Observer {
                val statusWithBinding = it ?: return@Observer
                val status = statusWithBinding.status.result.toStatus
                binding.bindingMail = when(status) {
                    ComponentRepository.StatusCode.OK -> {
                        adapter.updateBindingList(statusWithBinding.list)
                        binding.bindingNoInstances = statusWithBinding.list.isEmpty()
                        MailPackage.ok()
                    }
                    ComponentRepository.StatusCode.DOWNLOAD_IN_PROGRESS -> {
                        MailPackage.loading()
                    }
                    else -> MailPackage.error(status.errorMessage)
                }
                binding.executePendingBindings()
            })
        }

        fun setUpInputConnections() {
            val adapter = ConnectionAdapter(ConnectionAdapter.Direction.INPUT)
            RecyclerViewCosmetics.makeItAllWork(
                binding.insertInputConnectionsHere,
                adapter,
                ::deleteConnection,
                requireContext(),
                false,
                RecyclerViewCosmetics.LEFT
            )
            viewModel.liveInputConnectionV.observe(viewLifecycleOwner, Observer {
                val context = it ?: return@Observer
                val text = when(context) {
                    is OnlyStatus -> context.status.errorMessage
                    is ConnectionV -> {
                        adapter.updateConnectionList(context.connections)
                        binding.inputNoInstances = context.connections.isEmpty()
                        ""
                    }
                    else -> ComponentRepository.StatusCode.INTERNAL_ERROR.errorMessage
                }
                binding.inputMail = when(context.status) {
                    ComponentRepository.StatusCode.OK -> MailPackage.ok()
                    ComponentRepository.StatusCode.DOWNLOAD_IN_PROGRESS -> MailPackage.loading()
                    else -> MailPackage.error(text)
                }
                binding.executePendingBindings()
            })
        }

        fun setUpOutputConnections() {
            val adapter = ConnectionAdapter(ConnectionAdapter.Direction.OUTPUT)
            RecyclerViewCosmetics.makeItAllWork(
                binding.insertOutputConnectionsHere,
                adapter,
                ::deleteConnection,
                requireContext(),
                false,
                RecyclerViewCosmetics.LEFT
            )
            viewModel.liveOutputConnectionV.observe(viewLifecycleOwner, Observer {
                val context = it ?: return@Observer
                val text = when(context) {
                    is OnlyStatus -> context.status.errorMessage
                    is ConnectionV -> {
                        adapter.updateConnectionList(context.connections)
                        binding.outputNoInstances = context.connections.isEmpty()
                        ""
                    }
                    else -> ComponentRepository.StatusCode.INTERNAL_ERROR.errorMessage
                }
                binding.outputMail = when(context.status) {
                    ComponentRepository.StatusCode.OK -> MailPackage.ok()
                    ComponentRepository.StatusCode.DOWNLOAD_IN_PROGRESS -> MailPackage.loading()
                    else -> MailPackage.error(text)
                }
                binding.executePendingBindings()
            })
        }

        setUpButtons()
        setUpInputConnections()
        setUpOutputConnections()
    }

    private fun logConnections(list: List<Pair<Connection, ConnectionV.ConnectionItem>>) {
        l("---------------------------------------------------------------------------")
        list.forEach {
            l(it.second)
        }
        l("---------------------------------------------------------------------------")
    }

    private fun addConnection(binding: Binding) {
        viewModel.addConnectionButton(binding)
        CreateConnectionDialog.appear(parentFragmentManager)
    }

    private fun deleteConnection(pair: Pair<Connection, ConnectionV.ConnectionItem>) {
        val job = viewModel.deleteConnection(pair.first)
        job.invokeOnCompletion {
            if (it != null) {
                return@invokeOnCompletion
            }
            Snackbar.make(
                binding.root,
                "${getString(R.string.connection_with)} ${pair.second.component} ${getString(R.string.has_been_deleted)}",
                Snackbar.LENGTH_LONG
            ).setAction(getString(R.string.undo)) {
                viewModel.undoLastDeleted()
            }.show()
        }
    }

    companion object {
        private val l = Injector.generateLogFunction(this)
        fun getInstance() = BindingFragment()
    }
}
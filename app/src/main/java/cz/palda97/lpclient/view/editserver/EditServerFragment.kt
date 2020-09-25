package cz.palda97.lpclient.view.editserver

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.FragmentEditServerBinding
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.view.MainActivity
import cz.palda97.lpclient.viewmodel.editserver.EditServerViewModel

class EditServerFragment : Fragment() {

    companion object {
        fun newInstance() =
            EditServerFragment()
    }

    private lateinit var viewModel: EditServerViewModel
    private lateinit var binding: FragmentEditServerBinding
    private lateinit var doneButton: Button

    private var done = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_server, container, false)
        val root = binding.root
        viewModel = ViewModelProvider(this).get(EditServerViewModel::class.java)
        setUpComponents()
        MainActivity.switchToFragment = R.id.navigation_settings
        return root
    }

    private fun setUpComponents() {
        fun setUpDoneButton() {
            doneButton = binding.saveServer
            doneButton.setOnClickListener {
                saveServer()
            }
            viewModel.saveSuccessful.observe(viewLifecycleOwner, Observer {
                val status = it ?: return@Observer
                if (status == EditServerViewModel.SaveStatus.WAITING)
                    return@Observer
                run {
                    if (status == EditServerViewModel.SaveStatus.OK) {
                        done = true
                        requireActivity().finish()
                    }
                    val messageForSnack: String = when (status) {
                        EditServerViewModel.SaveStatus.NAME -> getString(R.string.save_status_name)
                        EditServerViewModel.SaveStatus.URL -> getString(R.string.save_status_url)
                        EditServerViewModel.SaveStatus.OK -> ""
                        EditServerViewModel.SaveStatus.WAITING -> ""
                        EditServerViewModel.SaveStatus.EMPTY_NAME -> getString(R.string.save_status_empty_name)
                        EditServerViewModel.SaveStatus.EMPTY_URL -> getString(R.string.save_status_empty_url)
                        EditServerViewModel.SaveStatus.WORKING -> ""
                    }
                    if (messageForSnack.isEmpty())
                        return@Observer
                    Snackbar.make(binding.root, messageForSnack, Snackbar.LENGTH_LONG)
                        .setAnchorView(binding.editServerBottomButtons)
                        .setAction("Action", null).show()
                }
                viewModel.resetStatus()
            })
        }
        setUpDoneButton()
    }

    private fun saveServer() {
        saveTmpInstance()
        viewModel.saveServer()
    }

    private fun saveTmpInstance() {
        val name: String = binding.name.editText!!.text.toString()
        val url: String = binding.url.editText!!.text.toString()
        val notes: String = binding.notes.editText!!.text.toString()
        val active: Boolean = binding.activeSwitch.isChecked
        val tmpInstance =
            ServerInstance(
                name,
                url,
                active,
                notes
            )
        viewModel.tmpServer = tmpInstance
    }

    /*override fun onPause() {
        if (!done)
            saveTmpInstance()
        super.onPause()
    }*/
    override fun onPause() {
        saveTmpInstance()
        super.onPause()
    }

    override fun onResume() {
        val tmpInstance = viewModel.tmpServer
        binding.name.editText!!.setText(tmpInstance.name)
        binding.url.editText!!.setText(tmpInstance.url)
        binding.notes.editText!!.setText(tmpInstance.description)
        binding.activeSwitch.isChecked = tmpInstance.active
        super.onResume()
    }
}
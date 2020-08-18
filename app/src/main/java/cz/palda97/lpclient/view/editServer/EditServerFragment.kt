package cz.palda97.lpclient.view.editServer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.FragmentEditServerBinding
import cz.palda97.lpclient.model.ServerInstance
import cz.palda97.lpclient.viewmodel.EditServerViewModel

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
        return root
    }

    private fun setUpComponents() {
        fun setUpDoneButton() {
            doneButton = binding.saveServer
            doneButton.setOnClickListener {
                saveServer()
                requireActivity().finish()
            }
        }
        setUpDoneButton()
    }

    private fun saveServer() {
        saveTmpInstance()
        viewModel.saveServer()
        done = true
    }

    private fun saveTmpInstance() {
        val name: String = binding.name.editText!!.text.toString()
        val url: String = binding.url.editText!!.text.toString()
        val notes: String = binding.notes.editText!!.text.toString()
        val active: Boolean = binding.activeSwitch.isChecked
        val tmpInstance = ServerInstance(name, url, active, notes)
        viewModel.tmpServerInstance = tmpInstance
    }

    override fun onPause() {
        if (!done)
            saveTmpInstance()
        super.onPause()
    }

    override fun onResume() {
        val tmpInstance = viewModel.tmpServerInstance
        binding.name.editText!!.setText(tmpInstance.name)
        binding.url.editText!!.setText(tmpInstance.url)
        binding.notes.editText!!.setText(tmpInstance.description)
        binding.activeSwitch.isChecked = tmpInstance.active
        super.onResume()
    }
}
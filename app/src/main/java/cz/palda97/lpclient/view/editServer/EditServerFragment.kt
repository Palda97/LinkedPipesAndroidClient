package cz.palda97.lpclient.view.editServer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import cz.palda97.lpclient.R
import cz.palda97.lpclient.viewmodel.EditServerViewModel

class EditServerFragment : Fragment() {

    companion object {
        fun newInstance() =
            EditServerFragment()
    }

    private lateinit var viewModel: EditServerViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_edit_server, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(EditServerViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
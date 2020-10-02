package cz.palda97.lpclient.view.editserver

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Button
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.material.snackbar.Snackbar
import com.varvet.barcodereadersample.barcode.BarcodeCaptureActivity
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.FragmentEditServerBinding
import cz.palda97.lpclient.model.entities.server.ServerFactory
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.view.MainActivity
import cz.palda97.lpclient.viewmodel.editserver.EditServerViewModel

class EditServerFragment : Fragment() {

    companion object {
        private val TAG = Injector.tag(this)
        private fun l(msg: String) = Log.d(TAG, msg)
        fun newInstance() =
            EditServerFragment()
        private const val BARCODE_READER_REQUEST_CODE = 1
    }

    private lateinit var viewModel: EditServerViewModel
    private lateinit var binding: FragmentEditServerBinding
    private lateinit var doneButton: Button

    private var done = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

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

    private fun startQrScanner() {
        val intent = Intent(requireContext(), BarcodeCaptureActivity::class.java)
        startActivityForResult(intent, BARCODE_READER_REQUEST_CODE)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_editserver, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId) {
        R.id.qr_scan_item -> {
            startQrScanner()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun parseFromQrCode(json: String?) {
        val server = ServerFactory.fromJson(json)
        if (server == null) {
            Snackbar.make(binding.root, getString(R.string.server_not_parsed), Snackbar.LENGTH_LONG)
                .setAnchorView(binding.editServerBottomButtons)
                .show()
            return
        }
        viewModel.tmpServer = server
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == BARCODE_READER_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    val barcode = data.getParcelableExtra<Barcode>(BarcodeCaptureActivity.BarcodeObject)
                    //val p = barcode.cornerPoints
                    //textView.text = barcode.displayValue
                    l("code captured: ${barcode?.displayValue}")
                    parseFromQrCode(barcode?.displayValue)
                } else {
                    l("no barcode captured")
                    //textView.setText(R.string.no_barcode_captured)
                }
            } else {
                Log.d(TAG, "result code: ${CommonStatusCodes.getStatusCodeString(resultCode)}")
            }
        } else
            super.onActivityResult(requestCode, resultCode, data)
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

    private fun loadFromServerInstance(serverInstance: ServerInstance) {
        binding.name.editText!!.setText(serverInstance.name)
        binding.url.editText!!.setText(serverInstance.url)
        binding.notes.editText!!.setText(serverInstance.description)
        binding.activeSwitch.isChecked = serverInstance.active
    }

    override fun onResume() {
        loadFromServerInstance(viewModel.tmpServer)
        super.onResume()
    }
}
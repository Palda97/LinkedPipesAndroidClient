package cz.palda97.lpclient.view

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import cz.palda97.lpclient.R
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.viewmodel.settings.SettingsViewModel

/**
 * Class for tuning the dropdown with server that is used as a filter.
 */
object ServerDropDownMagic {

    /**
     * Make a dropdown contain active server instances and react to a server being selected.
     * @param context [Context]
     * @param settingsViewModel [SettingsViewModel]
     * @param lifecycleOwner Lifecycle owner that contains this dropdown.
     * @param setServerToFilter Function for setting a [server instance as a filter][cz.palda97.lpclient.model.repository.ServerRepository.serverToFilter].
     * @param serverToFilter Server instance that is currently used as a filter.
     * @param includeNoServerOption If the dropdown should contain option for not filtering.
     * @receiver Dropdown that will be configurated.
     */
    fun MaterialAutoCompleteTextView.setUpWithServers(
        context: Context,
        settingsViewModel: SettingsViewModel,
        lifecycleOwner: LifecycleOwner,
        setServerToFilter: (ServerInstance?) -> Unit,
        serverToFilter: ServerInstance?,
        includeNoServerOption: Boolean = true
    ) {
        val adapter = ArrayAdapter<String>(context, R.layout.dropdown_item_text_view)
        settingsViewModel.activeLiveServers.observe(lifecycleOwner, Observer {
            val mail = it ?: return@Observer
            if (!mail.isOk)
                return@Observer
            mail.mailContent!!
            adapter.clear()
            adapter.addAll(mail.mailContent.map(ServerInstance::name))
            if (includeNoServerOption)
                adapter.add("")
            adapter.notifyDataSetChanged()
            setAdapter(adapter)
        })
        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val server = settingsViewModel.findActiveServerByName(s.toString())
                setServerToFilter(server)
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        serverToFilter?.let {
            setText(it.name)
        }
        setOnItemClickListener { _, _, _, _ ->
            clearFocus()
        }
    }
}
package cz.palda97.lpclient.viewmodel.editserver

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cz.palda97.lpclient.model.ServerInstance

class ServerInstanceAttributeCheck(serverInstance: ServerInstance) {

    /**
     * Check for name validity.
     * Returns SaveStatus.WORKING when ok.
     */
    val status: EditServerViewModel.SaveStatus = with(serverInstance) {
        when {
            name.isEmpty() -> EditServerViewModel.SaveStatus.EMPTY_NAME
            url.isEmpty() -> EditServerViewModel.SaveStatus.EMPTY_URL
            else -> EditServerViewModel.SaveStatus.WORKING
        }
    }
}
package cz.palda97.lpclient.viewmodel.editserver

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cz.palda97.lpclient.model.ServerInstance

class ServerInstanceAttributeCheck(val serverInstance: ServerInstance) {
    val isNameAndUrlOk: Boolean
        get() = serverInstance.name.isNotEmpty() && serverInstance.url.isNotEmpty()
    val liveData: LiveData<EditServerViewModel.SaveStatus>
        get() = when {
            serverInstance.name.isEmpty() -> MutableLiveData(EditServerViewModel.SaveStatus.EMPTY_NAME)
            serverInstance.url.isEmpty() -> MutableLiveData(EditServerViewModel.SaveStatus.EMPTY_URL)
            else -> MutableLiveData(EditServerViewModel.SaveStatus.WAITING)
        }
}
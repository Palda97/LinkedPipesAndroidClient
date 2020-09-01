package cz.palda97.lpclient.model.repository

import androidx.lifecycle.MutableLiveData
import cz.palda97.lpclient.model.ServerInstance

class EditServerRepository {
    var serverToEdit = ServerInstance()
    var tmpServer = ServerInstance()
}
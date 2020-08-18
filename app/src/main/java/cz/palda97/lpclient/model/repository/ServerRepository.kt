package cz.palda97.lpclient.model.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.ServerInstance

abstract class ServerRepository {
    abstract val liveServers: LiveData<MailPackage<List<ServerInstance>>>
    abstract val serverToEdit: MutableLiveData<ServerInstance>
    abstract fun saveServer(serverInstance: ServerInstance)
}
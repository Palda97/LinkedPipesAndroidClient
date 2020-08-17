package cz.palda97.lpclient.model.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.ServerInstance

class ServerRepositoryFake : ServerRepository() {

    private val serverList: List<ServerInstance> = listOf(
        ServerInstance("Home server", "192.168.1.10"),
        ServerInstance("Work server", "10.0.42.111"),
        ServerInstance("Test server", "192.168.1.11")
    )
    private val mailPackage = MailPackage(serverList, MailPackage.OK, "")
    private val _liveServers = MutableLiveData(mailPackage)

    override val liveServers: LiveData<MailPackage<List<ServerInstance>>>
        get() = _liveServers

    override val serverToEdit: MutableLiveData<ServerInstance> = MutableLiveData(ServerInstance())
}
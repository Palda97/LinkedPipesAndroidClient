package cz.palda97.lpclient.model.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.ServerInstance
import cz.palda97.lpclient.model.StatusPackage

class ServerRepositoryFake : ServerRepository() {

    private val serverList: List<ServerInstance> = listOf(
        ServerInstance("Home server", "192.168.1.10"),
        ServerInstance("Work server", "10.0.42.111"),
        ServerInstance("Test server", "192.168.1.11")
    )
    private val mailPackage = MailPackage(serverList)
    private val _liveServers = MutableLiveData(mailPackage)

    override val liveServers: LiveData<MailPackage<List<ServerInstance>>>
        get() = _liveServers

    override val serverToEdit: MutableLiveData<ServerInstance> = MutableLiveData(ServerInstance())
    override fun saveServer(serverInstance: ServerInstance) {
        _liveServers.value = MailPackage(_liveServers.value!!.mailContent!! + serverInstance)
    }

    override fun findServerByUrl(url: String): LiveData<MailPackage<ServerInstance>> {
        val list = _liveServers.value!!.mailContent!!
        list.forEach {
            if (it.url == url)
                return MutableLiveData(MailPackage(it))
        }
        return MutableLiveData(MailPackage.brokenPackage())
    }

    /*override fun matchingUrlAndName(serverInstance: ServerInstance): LiveData<MailPackage<MatchCases>> {
        val list = _liveServers.value!!.mailContent!!
        list.forEach {
            if (it.url == serverInstance.url)
                return MutableLiveData(MailPackage(MatchCases.URL))
            if (it.name == serverInstance.name)
                return MutableLiveData(MailPackage(MatchCases.NAME))
        }
        return MutableLiveData(MailPackage.brokenPackage())
    }*/
    override fun matchingUrlAndName(serverInstance: ServerInstance): LiveData<MailPackage<MatchCases>> {
        val live: MutableLiveData<MailPackage<MatchCases>> = MutableLiveData(MailPackage.loadingPackage())
        val list = _liveServers.value!!.mailContent!!
        val t = Thread {
            Thread.sleep(2000)
            list.forEach {
                if (it.url == serverInstance.url) {
                    live.postValue(MailPackage(MatchCases.URL))
                    return@Thread
                }
                if (it.name == serverInstance.name) {
                    live.postValue(MailPackage(MatchCases.NAME))
                    return@Thread
                }
            }
            live.postValue(MailPackage.brokenPackage())
        }
        t.start()
        return live
    }
}
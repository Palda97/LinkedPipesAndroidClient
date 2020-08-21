package cz.palda97.lpclient.model.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.ServerInstance
import cz.palda97.lpclient.model.StatusPackage

abstract class ServerRepository {
    abstract val liveServers: LiveData<MailPackage<List<ServerInstance>>>
    abstract val serverToEdit: MutableLiveData<ServerInstance>
    abstract fun saveServer(serverInstance: ServerInstance)
    abstract fun findServerByUrl(url: String): LiveData<MailPackage<ServerInstance>>
    enum class MatchCases {
        URL, NAME
    }

    abstract fun matchingUrlAndName(serverInstance: ServerInstance): LiveData<MailPackage<MatchCases>>
}
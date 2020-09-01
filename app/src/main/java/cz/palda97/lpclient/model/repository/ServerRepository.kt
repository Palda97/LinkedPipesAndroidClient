package cz.palda97.lpclient.model.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.ServerInstance
import cz.palda97.lpclient.model.StatusPackage

abstract class ServerRepository {
    abstract suspend fun insertServer(serverInstance: ServerInstance)
    enum class MatchCases {
        NO_MATCH, URL, NAME
    }
    abstract suspend fun matchUrlOrNameExcept(serverInstance: ServerInstance, except: ServerInstance): MatchCases
    suspend fun matchUrlOrName(serverInstance: ServerInstance) = matchUrlOrNameExcept(serverInstance, ServerInstance())
    abstract val liveServers: LiveData<MailPackage<List<ServerInstance>>>
    abstract suspend fun deleteAll()
    abstract suspend fun deleteServer(serverInstance: ServerInstance)
}
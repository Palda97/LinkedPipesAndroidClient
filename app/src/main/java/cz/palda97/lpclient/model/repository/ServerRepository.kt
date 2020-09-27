package cz.palda97.lpclient.model.repository

import androidx.lifecycle.LiveData
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.entities.server.ServerInstance

abstract class ServerRepository {
    abstract suspend fun insertServer(serverInstance: ServerInstance)
    enum class MatchCases {
        NO_MATCH, URL, NAME
    }
    abstract suspend fun matchUrlOrNameExcept(serverInstance: ServerInstance, except: ServerInstance): MatchCases
    suspend fun matchUrlOrName(serverInstance: ServerInstance) = matchUrlOrNameExcept(serverInstance,
        ServerInstance()
    )
    abstract val liveServers: LiveData<MailPackage<List<ServerInstance>>>
    abstract val activeLiveServers: LiveData<MailPackage<List<ServerInstance>>>
    abstract suspend fun deleteAll()
    abstract suspend fun deleteServer(serverInstance: ServerInstance)
    abstract var serverToFilter: ServerInstance?
}
package cz.palda97.lpclient.model.repository

import androidx.lifecycle.LiveData
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.db.dao.ServerInstanceDao
import cz.palda97.lpclient.model.entities.server.ServerInstance
import androidx.lifecycle.Transformations
import cz.palda97.lpclient.Injector

class ServerRepository(private val serverInstanceDao: ServerInstanceDao) {

    companion object {
        private val l = Injector.generateLogFunction(this)
    }

    suspend fun insertServer(serverInstance: ServerInstance) {
        val id = serverInstanceDao.insertServer(serverInstance)
        serverInstance.id = id
        l("insertServer: id: ${serverInstance.id.toString()}")
        if (serverInstance.active)
            Injector.repositoryRoutines.update(serverInstance)
    }

    suspend fun matchUrlOrNameExcept(
        serverInstance: ServerInstance,
        except: ServerInstance
    ): MatchCases {
        val list = serverInstanceDao.matchExcept(serverInstance.url, serverInstance.name, except.url, except.name)
        if (list.isEmpty())
            return MatchCases.NO_MATCH
        return if (list.first().url == serverInstance.url) MatchCases.URL else MatchCases.NAME
    }

    val liveServers: LiveData<MailPackage<List<ServerInstance>>> = Transformations.map(serverInstanceDao.serverList()) {
        if (it == null)
            return@map MailPackage.loadingPackage<List<ServerInstance>>()
        return@map MailPackage(it)
    }
    val activeLiveServers: LiveData<MailPackage<List<ServerInstance>>> = Transformations.map(serverInstanceDao.activeServersOnly()) {
        if (it == null)
            return@map MailPackage.loadingPackage<List<ServerInstance>>()
        return@map MailPackage(it)
    }

    suspend fun deleteAll() {
        serverInstanceDao.deleteAll()
    }

    suspend fun deleteServer(serverInstance: ServerInstance) {
        serverInstanceDao.deleteServer(serverInstance)
    }

    var serverToFilter: ServerInstance? = null

    enum class MatchCases {
        NO_MATCH, URL, NAME
    }

    suspend fun matchUrlOrName(serverInstance: ServerInstance) =
        matchUrlOrNameExcept(serverInstance, ServerInstance())

    suspend fun activeServers(): List<ServerInstance> = serverInstanceDao.activeServers()
}
package cz.palda97.lpclient.model.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.ServerInstance
import cz.palda97.lpclient.model.db.dao.ServerInstanceDao

class ServerRepositoryImp(private val serverInstanceDao: ServerInstanceDao) : ServerRepository() {
    override suspend fun insertServer(serverInstance: ServerInstance) {
        serverInstanceDao.insertServer(serverInstance)
    }

    override suspend fun matchUrlOrNameExcept(
        serverInstance: ServerInstance,
        except: ServerInstance
    ): MatchCases {
        val list = serverInstanceDao.matchExcept(serverInstance.url, serverInstance.name, except.url, except.name)
        if (list.isEmpty())
            return MatchCases.NO_MATCH
        return if (list.first().url == serverInstance.url) MatchCases.URL else MatchCases.NAME
    }

    override val liveServers: LiveData<MailPackage<List<ServerInstance>>> = Transformations.map(serverInstanceDao.serverList()) {
        if (it == null)
            return@map MailPackage.loadingPackage<List<ServerInstance>>()
        return@map MailPackage(it)
    }
    override val activeLiveServers: LiveData<MailPackage<List<ServerInstance>>> = Transformations.map(serverInstanceDao.activeServersOnly()) {
        if (it == null)
            return@map MailPackage.loadingPackage<List<ServerInstance>>()
        return@map MailPackage(it)
    }

    override suspend fun deleteAll() {
        serverInstanceDao.deleteAll()
    }

    override suspend fun deleteServer(serverInstance: ServerInstance) {
        serverInstanceDao.deleteServer(serverInstance)
    }

    override var serverToFilter: ServerInstance? = null
}
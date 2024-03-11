package cz.palda97.lpclient.model.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.db.dao.ServerInstanceDao
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.Injector

/**
 * Repository for working with [ServerInstances][ServerInstance].
 */
class ServerRepository(private val serverInstanceDao: ServerInstanceDao) {

    companion object {
        private val l = Injector.generateLogFunction(this)
    }

    /**
     * Insert [server][ServerInstance] to database and if it's active, call [RepositoryRoutines.update].
     */
    suspend fun insertServer(serverInstance: ServerInstance): ServerInstance {
        val id = serverInstanceDao.insertServer(serverInstance)
        serverInstance.id = id
        l("insertServer: id: ${serverInstance.id.toString()}")
        return serverInstance
    }

    /**
     * Search for [servers][ServerInstance] that have the same name or url as one server,
     * but not as the other one.
     * @return [Match case][MatchCases] according to what has been found.
     */
    suspend fun matchUrlOrNameExcept(
        serverInstance: ServerInstance,
        except: ServerInstance
    ): MatchCases {
        val list = serverInstanceDao.matchExcept(serverInstance.url, serverInstance.name, except.url, except.name)
        if (list.isEmpty())
            return MatchCases.NO_MATCH
        return if (list.first().url == serverInstance.url) MatchCases.URL else MatchCases.NAME
    }

    /**
     * LiveData with all server instances.
     */
    val liveServers: LiveData<MailPackage<List<ServerInstance>>> = serverInstanceDao.serverList().map {
        if (it == null)
            return@map MailPackage.loadingPackage<List<ServerInstance>>()
        return@map MailPackage(it)
    }

    /**
     * LiveData with all active server instances.
     */
    val activeLiveServers: LiveData<MailPackage<List<ServerInstance>>> = serverInstanceDao.activeServersOnly().map {
        if (it == null)
            return@map MailPackage.loadingPackage<List<ServerInstance>>()
        return@map MailPackage(it)
    }

    /**
     * Deletes all servers from database.
     */
    suspend fun deleteAll() {
        serverInstanceDao.deleteAll()
    }

    /**
     * Deletes the selected server from database.
     */
    suspend fun deleteServer(serverInstance: ServerInstance) {
        serverInstanceDao.deleteServer(serverInstance)
    }

    /**
     * A [ServerInstance] used as a filter for viewing purposes.
     */
    var serverToFilter: ServerInstance? = null

    enum class MatchCases {
        NO_MATCH, URL, NAME
    }

    /**
     * Gets a list of all active server instances.
     */
    suspend fun activeServers(): List<ServerInstance> = serverInstanceDao.activeServers()

    /**
     * Generate a server name that is not in database.
     */
    suspend fun nextAvailableName(domain: String): String {
        val similar = serverInstanceDao.selectByNameStart(domain).map { it.name }
        if (!similar.contains(domain))
            return domain
        tailrec fun loop(rank: Int = 2): String {
            val name = "$domain ($rank)"
            if (!similar.contains(name))
                return name
            return loop(rank + 1)
        }
        return loop()
    }
}
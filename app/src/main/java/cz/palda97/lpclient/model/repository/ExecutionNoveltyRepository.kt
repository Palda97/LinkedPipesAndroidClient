package cz.palda97.lpclient.model.repository

import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.db.dao.ExecutionDao
import cz.palda97.lpclient.model.db.dao.ExecutionNoveltyDao
import cz.palda97.lpclient.model.db.dao.ServerInstanceDao
import cz.palda97.lpclient.model.entities.execution.Execution
import cz.palda97.lpclient.model.entities.execution.Execution.Companion.areDone
import cz.palda97.lpclient.model.entities.execution.ExecutionNovelty
import cz.palda97.lpclient.model.entities.server.ServerInstance
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * Repository for working with [execution novelties][ExecutionNovelty].
 * This repository processes information about which executions ended recently,
 * so the application can decide if it should show notifications for them.
 */
class ExecutionNoveltyRepository(
    private val serverDao: ServerInstanceDao,
    private val noveltyDao: ExecutionNoveltyDao,
    private val executionDao: ExecutionDao
) {

    private data class ServersExecutionsTombstones(
        val servers: List<ServerInstance>,
        val executions: List<Execution>,
        val tombstones: List<String>
    )

    private suspend fun downloadExecutions(serversForDownloading: List<ServerInstance>): ServersExecutionsTombstones = coroutineScope {
        val executionRepo = Injector.executionRepository
        val jobs = serversForDownloading.map {
            async { executionRepo.downloadExecutions(it, true) }
        }
        val list = jobs.mapNotNull { it.await().mailContent }
        val servers = list.map { it.first.server }
        val executions = list.flatMap { it.first.executionList }.map { it.execution }
        val tombstones = list.flatMap { it.second }
        return@coroutineScope ServersExecutionsTombstones(servers, executions, tombstones)
    }

    private suspend fun downloadExecutions(): ServersExecutionsTombstones {
        val servers = serverDao.activeServers()
        return downloadExecutions(servers)
    }

    private suspend fun insertData(data: ServersExecutionsTombstones) {
        serverDao.insertServer(data.servers)
        executionDao.insert(data.executions)
        executionDao.delete(data.tombstones)
    }

    /**
     * Create and store [ExecutionNovelty] for executions which ended
     * and [ExecutionNovelty] have not been previously created for them.
     * @return Executions which ended and are new to the application.
     */
    suspend fun cacheNovelties(executions: List<Execution>): List<Execution> {
        val novelties = executions.areDone.map {
            ExecutionNovelty(it.id)
        }
        return noveltyDao.filterReallyNew(novelties).mapNotNull { it.execution }
    }

    /**
     * Download latest execution changes from active servers, store successfully
     * gathered information and proceed with [cacheNovelties].
     * @return Output from [cacheNovelties], called with downloaded executions.
     */
    suspend fun cacheNovelties(): List<Execution> {
        val downloadedData = downloadExecutions()
        insertData(downloadedData)
        return cacheNovelties(downloadedData.executions)
    }

    /**
     * Remove [execution novelties][ExecutionNovelty],
     * that miss matching [execution][Execution],
     * from database.
     */
    suspend fun cleanDb() {
        val toDelete = noveltyDao.selectNoveltyWithExecutionList().mapNotNull {
            if (it.execution == null) it.novelty else null
        }
        noveltyDao.delete(toDelete)
    }

    companion object {
        private val l = Injector.generateLogFunction(this)
    }
}
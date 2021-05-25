package cz.palda97.lpclient.model.repository

import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.db.dao.ExecutionNoveltyDao
import cz.palda97.lpclient.model.db.dao.ServerInstanceDao
import cz.palda97.lpclient.model.entities.execution.Execution
import cz.palda97.lpclient.model.entities.execution.ExecutionNovelty
import cz.palda97.lpclient.model.entities.execution.NoveltyWithExecution
import cz.palda97.lpclient.model.entities.server.ServerInstance
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class ExecutionNoveltyRepository(
    private val serverDao: ServerInstanceDao,
    private val noveltyDao: ExecutionNoveltyDao
) {

    private suspend fun downloadExecutions(servers: List<ServerInstance>): List<Execution> = coroutineScope {
        val executionRepo = Injector.executionRepository
        val jobs = servers.map {
            async { executionRepo.downloadExecutions(it) }
        }
        jobs.mapNotNull {
            it.await().mailContent?.executionList
        }.flatten().map { it.execution }
    }

    suspend fun downloadExecutions(): List<Execution> {
        val servers = serverDao.activeServers()
        return downloadExecutions(servers)
    }

    suspend fun filterReallyNew(executions: List<Execution>): List<NoveltyWithExecution> {
        val novelties = executions.map {
            ExecutionNovelty(it.id)
        }
        return noveltyDao.filterReallyNew(novelties)
    }

    suspend fun insertExecutions(executions: List<Execution>) {
        noveltyDao.insertExecutions(executions)
    }

    companion object {
        private val l = Injector.generateLogFunction(this)
    }
}
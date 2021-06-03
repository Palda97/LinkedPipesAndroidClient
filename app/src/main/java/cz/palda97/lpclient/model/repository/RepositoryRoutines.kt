package cz.palda97.lpclient.model.repository

import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.entities.execution.Execution
import cz.palda97.lpclient.model.entities.server.ServerInstance
import kotlinx.coroutines.*

/**
 * Processes that are needed to happen in more repositories.
 */
class RepositoryRoutines {

    private val serverRepository: ServerRepository = Injector.serverRepository
    private val pipelineViewRepository: PipelineViewRepository = Injector.pipelineViewRepository
    private val executionRepository: ExecutionRepository = Injector.executionRepository
    private val possibleComponentRepository: PossibleComponentRepository = Injector.possibleComponentRepository
    private val noveltyRepository: ExecutionNoveltyRepository = Injector.executionNoveltyRepository

    companion object {
        private val l = Injector.generateLogFunction(this)

        const val SERVER_NOT_FOUND = "SERVER_NOT_FOUND"
        const val INTERNAL_ERROR = "INTERNAL_ERROR"
    }

    /**
     * Tell repositories to update their content, but not so aggressively
     * (E.g. Don't display errors).
     * Called when active server is added, or an old server is now active.
     */
    suspend fun update(serverInstance: ServerInstance): List<Execution> = withContext(Dispatchers.IO) {
        val executionJob = async { executionRepository.update(serverInstance) }
        listOf(
            launch { pipelineViewRepository.update(serverInstance) },
            //launch { executionRepository.update(serverInstance) },
            launch { possibleComponentRepository.cachePossibleComponents(serverInstance) }
        ).forEach {
            it.join()
        }
        executionJob.await()
    }

    /**
     * Tell repositories to update their content.
     * Called when the refresh button is clicked and at the application start.
     */
    suspend fun refresh(): List<Execution> = withContext(Dispatchers.IO) {
        val servers = serverRepository.activeServers()
        val executionJob = async { executionRepository.cacheExecutions(Either.Right<ServerInstance, List<ServerInstance>?>(servers), false) }
        listOf(
            launch { pipelineViewRepository.refreshPipelineViews(Either.Right(servers)) },
            //launch { executionRepository.cacheExecutions(Either.Right<ServerInstance, List<ServerInstance>?>(servers), false) },
            launch { possibleComponentRepository.cachePossibleComponents(servers) }
        ).forEach {
            it.join()
        }
        executionJob.await()
    }

    /**
     * Pair [marks][cz.palda97.lpclient.model.db.MarkForDeletion] with contents of repositories
     * and send delete requests.
     * Called on application start.
     */
    suspend fun cleanDb() = withContext(Dispatchers.IO) {
        val jobs = listOf(
            launch {
                pipelineViewRepository.cleanDb()
                l("pipelineViewRepository.cleanDb() should be completed")
            },
            launch {
                executionRepository.cleanDb()
                l("executionRepository.cleanDb() should be completed")
            },
            launch {
                noveltyRepository.cleanDb()
                l("noveltyRepository.cleanDb() should be completed")
            }
        )
        jobs.forEach {
            it.join()
        }
    }

    /**
     * Inform repositories about changing a server that is used as a filter.
     * @see ServerRepository.serverToFilter
     */
    fun onServerToFilterChange() {
        pipelineViewRepository.onServerToFilterChange()
        executionRepository.onServerToFilterChange()
    }
}
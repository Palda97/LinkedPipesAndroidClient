package cz.palda97.lpclient.model.repository

import android.util.Log
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.entities.server.ServerInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RepositoryRoutines {

    private val serverRepository: ServerRepository = Injector.serverRepository
    private val pipelineViewRepository: PipelineViewRepository = Injector.pipelineViewRepository
    private val executionRepository: ExecutionRepository = Injector.executionRepository

    companion object {
        private val TAG = Injector.tag(this)
        private fun l(msg: String) = Log.d(TAG, msg)
    }

    fun update(serverInstance: ServerInstance) {
        CoroutineScope(Dispatchers.IO).launch {
            launch {
                pipelineViewRepository.update(serverInstance)
            }
            launch {
                executionRepository.update(serverInstance)
            }
        }
    }

    suspend fun refresh() = withContext(Dispatchers.IO) {
        val servers = serverRepository.activeServers()
        listOf(
            launch { pipelineViewRepository.refreshPipelineViews(Either.Right(servers)) },
            launch { executionRepository.cacheExecutions(Either.Right<ServerInstance, List<ServerInstance>?>(servers), false) }
        ).forEach {
            it.join()
        }
    }

    suspend fun cleanDb() = withContext(Dispatchers.IO) {
        val jobs = listOf(
            launch {
                pipelineViewRepository.cleanDb()
                l("pipelineViewRepository.cleanDb() should be completed")
            },
            launch {
                executionRepository.cleanDb()
                l("executionRepository.cleanDb() should be completed")
            }
        )
        jobs.forEach {
            it.join()
        }
    }

    fun onServerToFilterChange() {
        pipelineViewRepository.onServerToFilterChange()
        executionRepository.onServerToFilterChange()
    }
}
package cz.palda97.lpclient.viewmodel.executions

import android.app.Application
import androidx.lifecycle.*
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.*
import cz.palda97.lpclient.model.entities.execution.ServerWithExecutions
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.model.repository.ExecutionRepository
import cz.palda97.lpclient.model.repository.RepositoryRoutines
import cz.palda97.lpclient.model.repository.ServerRepository
import kotlinx.coroutines.*

class ExecutionsViewModel(application: Application) : AndroidViewModel(application) {

    private val executionRepository: ExecutionRepository = Injector.executionRepository
    private val serverRepository: ServerRepository = Injector.serverRepository

    private val retrofitScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.IO)
    private val dbScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.IO)

    private var lastSilent: Boolean = false

    val liveExecutions: LiveData<MailPackage<List<ExecutionV>>> =
        executionRepository.liveExecutions.switchMap {
            liveData(Dispatchers.Default) {
                l("update: ${it.status.name}")
                val mail = executionTransformation(it)
                lastSilent = false
                emit(mail)
            }
        }

    private suspend fun executionTransformation(it: MailPackage<List<ServerWithExecutions>>?): MailPackage<List<ExecutionV>> =
        withContext(Dispatchers.Default) {
            val mail = it ?: return@withContext MailPackage.loadingPackage<List<ExecutionV>>()
            return@withContext when (mail.status) {
                MailPackage.Status.OK -> {
                    mail.mailContent!!
                    val list = mail.mailContent.flatMap { serverWithExecutions ->
                        serverWithExecutions.executionList.filter {
                            !(it.mark != null || executionRepository.deleteRepo.toBeDeleted(it.execution))
                        }.map {
                            ExecutionV(it.execution.apply {
                                serverName = serverWithExecutions.server.name
                            })
                        }.sortedByDescending {
                            it.id
                        }
                    }
                    MailPackage(
                        list,
                        MailPackage.Status.OK,
                        if (lastSilent)
                            SCROLL
                        else
                            ""
                    )
                }
                MailPackage.Status.ERROR -> MailPackage.brokenPackage<List<ExecutionV>>(mail.msg)
                MailPackage.Status.LOADING -> MailPackage.loadingPackage<List<ExecutionV>>()
            }
        }

    private fun onServerToFilterChange() {
        //executionRepository.onServerToFilterChange()
        RepositoryRoutines().onServerToFilterChange()
    }

    var serverToFilter: ServerInstance?
        get() = serverRepository.serverToFilter
        private set(value) {
            val changed = value != serverRepository.serverToFilter
            serverRepository.serverToFilter = value
            if (changed) {
                onServerToFilterChange()
            }
        }

    fun setServerToFilterFun(serverInstance: ServerInstance?) {
        serverToFilter = serverInstance
    }

    private suspend fun downloadAllExecutions(silent: Boolean = false) {
        executionRepository.cacheExecutions(Either.Right(serverRepository.activeLiveServers.value?.mailContent), silent)
    }

    fun refreshExecutionsButton() {
        retrofitScope.launch {
            //downloadAllExecutions()
            RepositoryRoutines().refresh()
        }
    }

    private suspend fun deleteRoutine(executionV: ExecutionV) {
        val execution = executionRepository.find(executionV.id) ?: return
        executionRepository.markForDeletion(execution)
        executionRepository.deleteRepo.addPending(execution, DELETE_DELAY)
    }

    fun deleteExecution(executionV: ExecutionV) {
        retrofitScope.launch {
            deleteRoutine(executionV)
        }
    }

    private suspend fun cancelRoutine(executionV: ExecutionV) {
        val execution = executionRepository.find(executionV.id) ?: return
        executionRepository.unMarkForDeletion(execution)
        executionRepository.deleteRepo.cancelDeletion(execution)
    }

    fun cancelDeletion(executionV: ExecutionV) {
        retrofitScope.launch {
            cancelRoutine(executionV)
        }
    }

    fun silentRefresh() {
        lastSilent = true
        retrofitScope.launch {
            downloadAllExecutions(true)
        }
    }

    companion object {
        private val l = Injector.generateLogFunction(this)
        private const val DELETE_DELAY: Long = 5000L
        const val SCROLL = "SCROLL"
    }
}
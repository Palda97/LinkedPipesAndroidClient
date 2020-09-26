package cz.palda97.lpclient.viewmodel.executions

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.*
import cz.palda97.lpclient.model.entities.execution.ServerWithExecutions
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.model.repository.ExecutionRepository
import cz.palda97.lpclient.model.repository.ServerRepository
import kotlinx.coroutines.*

class ExecutionsViewModel(application: Application) : AndroidViewModel(application) {

    private val executionRepository: ExecutionRepository = Injector.executionRepository
    private val serverRepository: ServerRepository = Injector.serverRepository

    private val retrofitScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.IO)

    val liveExecutions: LiveData<MailPackage<List<ExecutionV>>> =
        executionRepository.liveExecutions.switchMap {
            liveData(Dispatchers.Default) {
                emit(MailPackage.loadingPackage())
                val mail = executionTransformation(it)
                emit(mail)
            }
        }

    private suspend fun executionTransformation(it: MailPackage<List<ServerWithExecutions>>?): MailPackage<List<ExecutionV>> =
        withContext(Dispatchers.Default) {
            val mail = it ?: return@withContext MailPackage.loadingPackage<List<ExecutionV>>()
            return@withContext when(mail.status) {
                MailPackage.Status.OK -> {
                    mail.mailContent!!
                    val list = mail.mailContent.flatMap {serverWithExecutions ->
                        serverWithExecutions.executionList.filter {
                            !it.deleted
                        }.map {
                            ExecutionV(it)
                            /*with(it) {
                                ExecutionV(id, serverWithExecutions.server.name, pipelineName, ExecutionDateParser.toViewFormat(start), status)
                            }*/
                        }
                    }
                    MailPackage(list)
                }
                MailPackage.Status.ERROR -> MailPackage.brokenPackage<List<ExecutionV>>(mail.msg)
                MailPackage.Status.LOADING -> MailPackage.loadingPackage<List<ExecutionV>>()
            }
        }

    private fun onServerToFilterChange() {
        executionRepository.onServerToFilterChange()
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

    private suspend fun downloadAllExecutions() {
        executionRepository.cacheExecutions(serverRepository.activeLiveServers.value?.mailContent)
    }

    fun refreshExecutionsButton() {
        retrofitScope.launch {
            downloadAllExecutions()
        }
    }

    companion object {
        private val TAG = Injector.tag(this)
        private fun l(msg: String) = Log.d(TAG, msg)
    }
}
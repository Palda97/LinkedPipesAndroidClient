package cz.palda97.lpclient.model.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.db.dao.ExecutionDao
import cz.palda97.lpclient.model.db.dao.MarkForDeletionDao
import cz.palda97.lpclient.model.db.dao.ServerInstanceDao
import cz.palda97.lpclient.model.entities.execution.*
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.model.network.ExecutionRetrofit
import cz.palda97.lpclient.model.network.ExecutionRetrofit.Companion.executionRetrofit
import cz.palda97.lpclient.model.network.RetrofitHelper
import cz.palda97.lpclient.model.services.ExecutionMonitor
import kotlinx.coroutines.*

class ExecutionRepository(
    private val executionDao: ExecutionDao,
    private val serverDao: ServerInstanceDao,
    private val deleteDao: MarkForDeletionDao
) {

    private fun executionFilterTransformation(it: List<ServerWithExecutions>?): MailPackage<List<ServerWithExecutions>> {
        if (it == null)
            return MailPackage.loadingPackage()
        val serverRepo = Injector.serverRepository
        val serverToFilter = serverRepo.serverToFilter ?: return MailPackage(it)
        val filtered = it.find { it.server == serverToFilter }
            ?: return MailPackage.brokenPackage("The right server not fund: ${serverToFilter.name}")
        return MailPackage(listOf(filtered))
    }

    private val dbMirror = serverDao.activeServerListWithExecutions()

    private val filteredLiveExecutions: LiveData<MailPackage<List<ServerWithExecutions>>> =
        Transformations.map(dbMirror) {
            return@map executionFilterTransformation(it)
        }

    private val mediator = MediatorLiveData<MailPackage<List<ServerWithExecutions>>>().apply {
        addSource(filteredLiveExecutions) {
            if (!noisyFlag)
                postValue(it)
        }
    }

    val liveExecutions: LiveData<MailPackage<List<ServerWithExecutions>>>
        get() = mediator

    fun onServerToFilterChange() {
        mediator.postValue(executionFilterTransformation(dbMirror.value))
    }

    enum class StatusCode {
        NO_CONNECT, SERVER_ID_INVALID, NOT_FOUND_ON_SERVER, OK, ERROR
    }

    private suspend fun getExecutionRetrofit(server: ServerInstance): Either<StatusCode, ExecutionRetrofit> =
        try {
            //Either.Right(ExecutionRetrofit.getInstance(server.url))
            Either.Right(RetrofitHelper.getBuilder(server, server.frontendUrl).executionRetrofit)
        } catch (e: IllegalArgumentException) {
            l("getExecutionRetrofit ${e.toString()}")
            Either.Left(StatusCode.NO_CONNECT)
        }

    private suspend fun downloadExecutions(server: ServerInstance): MailPackage<ServerWithExecutions> {
        val retrofit = when (val res = getExecutionRetrofit(server)) {
            is Either.Left -> return MailPackage.brokenPackage(res.value.name)
            is Either.Right -> res.value
        }
        val call = retrofit.executionList()
        val text = RetrofitHelper.getStringFromCall(call)
        return ExecutionFactory(server, text).serverWithExecutions
    }

    private suspend fun downloadExecutions(serverList: List<ServerInstance>?): MailPackage<List<ServerWithExecutions>> =
        coroutineScope {
            if (serverList == null)
                return@coroutineScope MailPackage.brokenPackage<List<ServerWithExecutions>>("server list is null")
            val jobs = serverList.map {
                async {
                    downloadExecutions(it) to it
                }
            }
            val list = jobs.map {
                val (mail, server) = it.await()
                if (!mail.isOk)
                    return@coroutineScope MailPackage.brokenPackage<List<ServerWithExecutions>>("error while parsing executions from ${server.name}")
                mail.mailContent!!
            }
            return@coroutineScope MailPackage(list)
        }

    private suspend fun updateDbAndRefresh(list: List<Execution>, silent: Boolean) {
        return when (silent) {
            true -> executionDao.silentInsert(list)
            false -> {
                noisyFlag = false
                if (list.isEmpty())
                    mediator.postValue(MailPackage(emptyList()))
                executionDao.renewal(list)
            }
        }
    }

    private suspend fun cacheExecutions(server: ServerInstance, silent: Boolean) {
        val mail = downloadExecutions(server)
        if (mail.isOk) {
            val list = mail.mailContent!!.executionList.map { it.execution }
            updateDbAndRefresh(list, silent)
        }
        if (mail.isError)
            mediator.postValue(MailPackage.brokenPackage(mail.msg))
    }

    private suspend fun cacheExecutions(serverList: List<ServerInstance>?, silent: Boolean) {
        val mail = downloadExecutions(serverList)
        if (mail.isOk) {
            val list = mail.mailContent!!.flatMap { it.executionList }.map { it.execution }
            updateDbAndRefresh(list, silent)
        }
        if (mail.isError)
            mediator.postValue(MailPackage.brokenPackage(mail.msg))
    }

    var noisyFlag = false

    suspend fun cacheExecutions(
        either: Either<ServerInstance, List<ServerInstance>?>,
        silent: Boolean
    ) {
        if (!silent) {
            noisyFlag = true
            mediator.postValue(MailPackage.loadingPackage())
        }
        return when (either) {
            is Either.Left -> cacheExecutions(either.value, silent)
            is Either.Right -> cacheExecutions(either.value, silent)
        }
    }

    suspend fun markForDeletion(execution: Execution) {
        deleteDao.markForDeletion(execution.id)
    }

    suspend fun unMarkForDeletion(execution: Execution) {
        deleteDao.unMarkForDeletion(execution.id)
    }

    suspend fun find(id: String): Execution? = executionDao.findById(id)

    private suspend fun deleteRoutine(execution: Execution) {
        executionDao.delete(execution)
        deleteDao.delete(execution.id)
    }

    private suspend fun deleteExecution(execution: Execution): StatusCode {
        val server = serverDao.findById(execution.serverId) ?: return StatusCode.SERVER_ID_INVALID
        val retrofit = when (val res = getExecutionRetrofit(server)) {
            is Either.Left -> return res.value
            is Either.Right -> res.value
        }
        val call = retrofit.delete(execution.idNumber)
        val text = RetrofitHelper.getStringFromCall(call)
        if (text == null) {
            //Not found on server
            deleteRoutine(execution)
            return StatusCode.NOT_FOUND_ON_SERVER
        }
        if (text.isEmpty()) {
            //Success
            deleteRoutine(execution)
            return StatusCode.OK
        }
        return StatusCode.ERROR
    }

    val deleteRepo = DeleteRepository<Execution> {
        deleteExecution(it)
    }

    suspend fun cleanDb() {
        executionDao.selectDeleted().forEach {
            deleteExecution(it)
        }
    }

    suspend fun update(server: ServerInstance) {
        val mail = downloadExecutions(server)
        if (!mail.isOk)
            return
        val pack = mail.mailContent!!
        executionDao.deleteByServer(pack.server.id)
        executionDao.insert(pack.executionList.map { it.execution })
    }

    private suspend fun getSpecificExecution(executionId: String, server: ServerInstance): String? {
        val retrofit = when (val res = getExecutionRetrofit(server)) {
            is Either.Left -> return null
            is Either.Right -> res.value
        }
        val call = retrofit.execution(Execution.idNumberFun(executionId))
        return RetrofitHelper.getStringFromCall(call)
    }

    suspend fun monitor(serverId: Long, executionId: String): ExecutionStatus? {
        //l("monitor thread: ${Thread.currentThread().name}")
        var finalStatus: ExecutionStatus? = null
        while (true) {
            delay(MONITOR_DELAY)
            val server = serverDao.findById(serverId) ?: break
            if (!server.active)
                break
            val json = getSpecificExecution(executionId, server)
            if (json == "[ ]") {
                continue
            }
            val status = ExecutionStatusUtilities.fromDirectRequest(json) ?: break
            executionDao.findById(executionId)?.let {
                if (status != it.status) {
                    it.status = status
                    executionDao.insert(it)
                }
            }
            if (status != ExecutionStatus.QUEUED && status != ExecutionStatus.RUNNING) {
                finalStatus = status
                break
            }
        }
        return finalStatus
    }

    companion object {
        private val TAG = Injector.tag(this)
        private fun l(msg: String) = Log.d(TAG, msg)
        private const val MONITOR_DELAY = 1000L
    }
}
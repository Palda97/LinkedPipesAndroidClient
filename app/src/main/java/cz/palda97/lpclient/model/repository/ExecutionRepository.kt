package cz.palda97.lpclient.model.repository

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
import kotlinx.coroutines.*

/**
 * Repository for working with [Executions][Execution].
 */
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
            //?: return MailPackage.brokenPackage("The right server not fund: ${serverToFilter.name}")
            ?: return MailPackage.brokenPackage(RepositoryRoutines.SERVER_NOT_FOUND)
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

    /**
     * LiveData containing executions that belong to a server that is used as a filter,
     * or all execution if no server is set as a filter. It is all wrapper in [MailPackage],
     * so it can even represent [loading][MailPackage.Status.LOADING] while the executions are being downloaded.
     */
    val liveExecutions: LiveData<MailPackage<List<ServerWithExecutions>>>
        get() = mediator

    /**
     * Update [liveExecutions] according to server that is used as a filter.
     * @see ServerRepository.serverToFilter
     */
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
                //return@coroutineScope MailPackage.brokenPackage<List<ServerWithExecutions>>("server list is null")
                return@coroutineScope MailPackage.brokenPackage<List<ServerWithExecutions>>(RepositoryRoutines.INTERNAL_ERROR)
            val jobs = serverList.map {
                async {
                    downloadExecutions(it) to it
                }
            }
            val list = jobs.map {
                val (mail, server) = it.await()
                if (!mail.isOk)
                    //return@coroutineScope MailPackage.brokenPackage<List<ServerWithExecutions>>("error while parsing executions from ${server.name}")
                    return@coroutineScope MailPackage.brokenPackage<List<ServerWithExecutions>>(server.name)
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

    /**
     * While this is true, [liveExecutions] is not responding for changes in database.
     */
    var noisyFlag = false

    /**
     * Download and store executions that belong to selected server(s).
     * @param either [Either] [ServerInstance] or list of them.
     * @param silent If executions should be updated [silently][ExecutionDao.silentInsert].
     */
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

    /**
     * Add a [mark][cz.palda97.lpclient.model.db.MarkForDeletion] of this execution to the database.
     */
    suspend fun markForDeletion(execution: Execution) {
        deleteDao.markForDeletion(execution.id)
    }

    /**
     * Unmark this execution.
     */
    suspend fun unMarkForDeletion(execution: Execution) {
        deleteDao.unMarkForDeletion(execution.id)
    }

    /**
     * Find execution by id.
     */
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

    /** @see DeleteRepository */
    val deleteRepo = DeleteRepository<Execution> {
        deleteExecution(it)
    }

    /**
     * Pair [marks][cz.palda97.lpclient.model.db.MarkForDeletion] with [executions][Execution]
     * and send delete requests.
     */
    suspend fun cleanDb() {
        executionDao.selectDeleted().forEach {
            deleteExecution(it)
        }
    }

    /**
     * Called when active server is added, or an old server is now active.
     * Tries to download executions. When successful, it updates the database,
     * otherwise it does nothing.
     */
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

    /**
     * Downloads the specific execution, fetches it's status and update the database.
     * @return [ExecutionStatus] of the execution, or null on error.
     */
    suspend fun fetchStatus(serverId: Long, executionId: String): ExecutionStatus? {
        while (true) {
            val server = serverDao.findById(serverId) ?: return null
            if (!server.active)
                return null
            val json = getSpecificExecution(executionId, server)
            if (json == "[ ]") {
                delay(MONITOR_DELAY)
                continue
            }
            val status = ExecutionStatusUtilities.fromDirectRequest(json) ?: return null
            executionDao.findById(executionId)?.let {
                if (status != it.status) {
                    it.status = status
                    executionDao.insert(it)
                }
            }
            return status
        }
    }

    companion object {
        private val l = Injector.generateLogFunction(this)
        private const val MONITOR_DELAY = 1000L
    }
}
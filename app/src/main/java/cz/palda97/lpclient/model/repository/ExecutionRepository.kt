package cz.palda97.lpclient.model.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.db.dao.ExecutionDao
import cz.palda97.lpclient.model.db.dao.ServerInstanceDao
import cz.palda97.lpclient.model.entities.execution.Execution
import cz.palda97.lpclient.model.entities.execution.ExecutionFactory
import cz.palda97.lpclient.model.entities.execution.ServerWithExecutions
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.model.network.ExecutionRetrofit
import cz.palda97.lpclient.model.network.RetrofitHelper
import cz.palda97.lpclient.viewmodel.executions.ExecutionV

class ExecutionRepository(
    private val executionDao: ExecutionDao,
    private val serverDao: ServerInstanceDao
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
            Either.Right(ExecutionRetrofit.getInstance(mixAddressWithPort(server.url)))
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

    private suspend fun downloadExecutions(serverList: List<ServerInstance>?): MailPackage<List<ServerWithExecutions>> {
        if (serverList == null)
            return MailPackage.brokenPackage("server list is null")
        val list: MutableList<ServerWithExecutions> = mutableListOf()
        serverList.forEach {
            val mail = downloadExecutions(it)
            if (!mail.isOk)
                return MailPackage.brokenPackage("error while parsing executions from ${it.name}")
            mail.mailContent!!
            list.add(mail.mailContent)
        }
        return MailPackage(list)
    }

    private suspend fun updateDbAndRefresh(list: List<Execution>) {
        if (list.isEmpty())
            mediator.postValue(MailPackage(emptyList()))
        executionDao.renewal(list)
    }

    private suspend fun cacheExecutions(server: ServerInstance) {
        val mail = downloadExecutions(server)
        if (mail.isOk) {
            val list = mail.mailContent!!.executionList
            updateDbAndRefresh(list)
        }
        if (mail.isError)
            mediator.postValue(MailPackage.brokenPackage(mail.msg))
    }

    private suspend fun cacheExecutions(serverList: List<ServerInstance>?) {
        val mail = downloadExecutions(serverList)
        if (mail.isOk) {
            val list = mail.mailContent!!.flatMap { it.executionList }
            updateDbAndRefresh(list)
        }
        if (mail.isError)
            mediator.postValue(MailPackage.brokenPackage(mail.msg))
    }

    suspend fun cacheExecutions(either: Either<ServerInstance, List<ServerInstance>?>) {
        mediator.postValue(MailPackage.loadingPackage())
        return when (either) {
            is Either.Left -> cacheExecutions(either.value)
            is Either.Right -> cacheExecutions(either.value)
        }
    }

    suspend fun markForDeletion(execution: ExecutionV) {
        executionDao.markForDeletion(execution.id)
    }

    suspend fun find(execution: ExecutionV): Execution? = executionDao.findById(execution.id)

    suspend fun deleteExecution(execution: Execution): StatusCode {
        val server = serverDao.findById(execution.serverId) ?: return StatusCode.SERVER_ID_INVALID
        val retrofit = when (val res = getExecutionRetrofit(server)) {
            is Either.Left -> return res.value
            is Either.Right -> res.value
        }
        val call = retrofit.delete(execution.idNumber)
        val text = RetrofitHelper.getStringFromCall(call)
        if (text == null) {
            //Not found on server
            executionDao.delete(execution)
            return StatusCode.NOT_FOUND_ON_SERVER
        }
        if (text.isEmpty()) {
            //Success
            executionDao.delete(execution)
            return StatusCode.OK
        }
        return StatusCode.ERROR
    }

    suspend fun unMarkForDeletion(execution: ExecutionV) {
        executionDao.unMarkForDeletion(execution.id)
    }

    companion object {
        private val TAG = Injector.tag(this)
        private fun l(msg: String) = Log.d(TAG, msg)
        private const val FRONTEND_PORT: Short = 8080
        private fun mixAddressWithPort(address: String) = "${address}:${FRONTEND_PORT}/"
    }
}
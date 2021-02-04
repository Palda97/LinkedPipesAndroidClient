package cz.palda97.lpclient.model.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.*
import cz.palda97.lpclient.model.db.dao.MarkForDeletionDao
import cz.palda97.lpclient.model.db.dao.PipelineViewDao
import cz.palda97.lpclient.model.db.dao.ServerInstanceDao
import cz.palda97.lpclient.model.entities.pipelineview.PipelineView
import cz.palda97.lpclient.model.entities.pipelineview.PipelineViewFactory
import cz.palda97.lpclient.model.entities.pipelineview.ServerWithPipelineViews
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.model.network.PipelineRetrofit
import cz.palda97.lpclient.model.network.PipelineRetrofit.Companion.pipelineRetrofit
import cz.palda97.lpclient.model.network.RetrofitHelper
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.io.IOException

class PipelineViewRepository(
    private val pipelineViewDao: PipelineViewDao,
    private val serverInstanceDao: ServerInstanceDao,
    private val deleteDao: MarkForDeletionDao
) {

    private val dbMirror = serverInstanceDao.activeServerListWithPipelineViews()

    val liveServersWithPipelineViews: MediatorLiveData<MailPackage<List<ServerWithPipelineViews>>> =
        MediatorLiveData()

    private val livePipelineViews: LiveData<MailPackage<List<ServerWithPipelineViews>>> =
        Transformations.map(dbMirror) {
            return@map pipelineViewsFilterTransformation(it)
        }

    init {
        with(liveServersWithPipelineViews) {
            addSource(livePipelineViews) {
                if (!noisyFlag)
                    postValue(it)
            }
        }
    }

    fun onServerToFilterChange() {
        liveServersWithPipelineViews.postValue(pipelineViewsFilterTransformation(dbMirror.value))
    }

    private fun pipelineViewsFilterTransformation(it: List<ServerWithPipelineViews>?): MailPackage<List<ServerWithPipelineViews>> {
        l("pipelineViewsFilterTransformation start")
        if (it == null)
            return MailPackage.loadingPackage<List<ServerWithPipelineViews>>()
        val serverRepo = Injector.serverRepository
        val serverToFilter = serverRepo.serverToFilter ?: return MailPackage(it)
        val serverWithPipelineViews = it.find { it.server == serverToFilter }
            ?: return MailPackage.brokenPackage<List<ServerWithPipelineViews>>("ServerWithPipelineViews not fund: ${serverToFilter.name}")
        return MailPackage(listOf(serverWithPipelineViews))
    }

    suspend fun insertPipelineView(pipelineView: PipelineView) {
        pipelineViewDao.insert(pipelineView)
    }

    private suspend fun deleteAndInsertPipelineViews(list: List<PipelineView>) {
        noisyFlag = false
        pipelineViewDao.deleteAndInsertPipelineViews(list)
    }

    private var noisyFlag = false

    suspend fun refreshPipelineViews(either: Either<ServerInstance, List<ServerInstance>?>) {
        noisyFlag = true
        liveServersWithPipelineViews.postValue(MailPackage.loadingPackage())
        when (either) {
            is Either.Left -> downAndCachePipelineViews(either.value)
            is Either.Right -> {
                if (either.value == null || either.value.isEmpty())
                    liveServersWithPipelineViews.postValue(MailPackage(emptyList()))
                downAndCachePipelineViews(either.value)
            }
        }
    }

    private suspend fun downAndCachePipelineViews(serverList: List<ServerInstance>?) {
        val mail = downloadPipelineViews(serverList)
        if (mail.isOk) {
            mail.mailContent!!
            if (mail.mailContent.flatMap { it.pipelineViewList }.isEmpty())
                liveServersWithPipelineViews.postValue(MailPackage(emptyList()))
            deleteAndInsertPipelineViews(mail.mailContent.flatMap { it.pipelineViewList }.map { it.pipelineView })
        }
        if (mail.isError)
            liveServersWithPipelineViews.postValue(mail)
    }

    private suspend fun downAndCachePipelineViews(serverInstance: ServerInstance) {
        val mail = downloadPipelineViews(serverInstance)
        if (mail.isOk) {
            mail.mailContent!!
            if (mail.mailContent.pipelineViewList.isEmpty())
                liveServersWithPipelineViews.postValue(MailPackage(emptyList()))
            deleteAndInsertPipelineViews(mail.mailContent.pipelineViewList.map { it.pipelineView })
        }
        if (mail.isError)
            liveServersWithPipelineViews.postValue(MailPackage.brokenPackage(mail.msg))
    }

    private suspend fun downloadPipelineViews(serverList: List<ServerInstance>?): MailPackage<List<ServerWithPipelineViews>> =
        coroutineScope {
            if (serverList == null)
                return@coroutineScope MailPackage.brokenPackage<List<ServerWithPipelineViews>>("server list is null")
            val jobs = serverList.map {
                async {
                    downloadPipelineViews(it) to it
                }
            }
            val list = jobs.map {
                val (mail, server) = it.await()
                if (!mail.isOk)
                    return@coroutineScope MailPackage.brokenPackage<List<ServerWithPipelineViews>>("error while parsing pipelines from ${server.name}")
                mail.mailContent!!
            }

            return@coroutineScope MailPackage(list)
        }

    suspend fun downloadPipelineViews(serverInstance: ServerInstance): MailPackage<ServerWithPipelineViews> {
        val pipelineRetrofit = when (val res = getPipelineRetrofit(serverInstance)) {
            is Either.Left -> {
                return when (res.value) {
                    StatusCode.NO_CONNECT -> MailPackage.brokenPackage(StatusCode.NO_CONNECT.name)
                    else -> MailPackage.brokenPackage(StatusCode.INTERNAL_ERROR.name)
                }
            }
            is Either.Right -> res.value
        }
        val call = pipelineRetrofit.pipelineList()
        val text = RetrofitHelper.getStringFromCall(call)
        return PipelineViewFactory(
            serverInstance,
            text
        ).serverWithPipelineViews
    }

    enum class StatusCode {
        SERVER_ID_NOT_FOUND, NO_CONNECT, NULL_RESPONSE, OK, INTERNAL_ERROR
    }

    suspend fun cleanDb() {
        pipelineViewDao.selectDeleted().forEach {
            deletePipeline(it)
        }
    }

    suspend fun getPipelineRetrofit(pipelineView: PipelineView): Either<StatusCode, PipelineRetrofit> {
        val server = serverInstanceDao.findById(pipelineView.serverId)
            ?: return Either.Left(StatusCode.SERVER_ID_NOT_FOUND)
        return getPipelineRetrofit(server)
    }

    suspend fun getPipelineRetrofit(server: ServerInstance): Either<StatusCode, PipelineRetrofit> =
        try {
            //Either.Right(PipelineRetrofit.getInstance(server.url))
            Either.Right(RetrofitHelper.getBuilder(server, server.frontendUrl).pipelineRetrofit)
        } catch (e: IllegalArgumentException) {
            l("deletePipeline ${e.toString()}")
            Either.Left(StatusCode.NO_CONNECT)
        }

    private suspend fun deletePipeline(pipelineView: PipelineView): StatusCode {
        val pipelineRetrofit = when (val res = getPipelineRetrofit(pipelineView)) {
            is Either.Left -> return res.value
            is Either.Right -> res.value
        }
        val call = pipelineRetrofit.deletePipeline(pipelineView.idNumber)
        val text = RetrofitHelper.getStringFromCall(call)
        if (text == null) { //Pipeline was already deleted
            l("deletePipeline text is null")
            deleteRoutine(pipelineView)
            return StatusCode.NULL_RESPONSE
        }
        if (text.isEmpty()) {//Deletion was successful
            l("text isEmpty")
            deleteRoutine(pipelineView)
            return StatusCode.OK
        }
        l("deletePipeline internall error - text = $text")
        return StatusCode.INTERNAL_ERROR
    }

    private suspend fun deleteRoutine(pipelineView: PipelineView) {
        pipelineViewDao.deletePipelineView(pipelineView)
        deleteDao.delete(pipelineView.id)
    }

    suspend fun findPipelineViewById(id: String): PipelineView? =
        pipelineViewDao.findPipelineViewById(id)

    suspend fun downloadPipelineString(pipelineView: PipelineView): Either<StatusCode, String> {
        val pipelineRetrofit = when (val res = getPipelineRetrofit(pipelineView)) {
            is Either.Left -> return Either.Left(res.value)
            is Either.Right -> res.value
        }
        val call = pipelineRetrofit.getPipeline(pipelineView.idNumber)
        //val text = RetrofitHelper.getStringFromCall(call) ?: return Either.Left(StatusCode.NULL_RESPONSE)
        val text = try {
            val executedCall = call.execute()
            if (executedCall.code() == 404)
                return Either.Left(StatusCode.NULL_RESPONSE)
            val response = executedCall.body()
            response?.string()
        } catch (e: IOException) {
            l("getStringFromCall ${e.toString()}")
            null
        }
            ?: return Either.Left(StatusCode.INTERNAL_ERROR)
        return Either.Right(text)
    }

    suspend fun markForDeletion(pipelineView: PipelineView) {
        deleteDao.markForDeletion(pipelineView.id)
    }

    suspend fun unMarkForDeletion(pipelineView: PipelineView) {
        deleteDao.unMarkForDeletion(pipelineView.id)
    }

    val deleteRepo = DeleteRepository<PipelineView> {
        deletePipeline(it)
    }

    suspend fun update(server: ServerInstance) {
        val mail = downloadPipelineViews(server)
        if (!mail.isOk)
            return
        val pack = mail.mailContent!!
        pipelineViewDao.deleteByServer(pack.server.id)
        pipelineViewDao.insertList(pack.pipelineViewList.map { it.pipelineView })
    }

    companion object {
        private val l = Injector.generateLogFunction(this)
    }
}
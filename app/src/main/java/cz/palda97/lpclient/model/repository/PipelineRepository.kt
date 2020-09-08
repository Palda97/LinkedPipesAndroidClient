package cz.palda97.lpclient.model.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.*
import cz.palda97.lpclient.model.db.dao.PipelineViewDao
import cz.palda97.lpclient.model.db.dao.ServerInstanceDao
import cz.palda97.lpclient.model.network.PipelineRetrofit
import cz.palda97.lpclient.model.network.RetrofitHelper

class PipelineRepository(
    private val pipelineViewDao: PipelineViewDao,
    private val serverInstanceDao: ServerInstanceDao
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
                postValue(it)
            }
        }
    }

    fun onServerToFilterChange() {
        liveServersWithPipelineViews.postValue(pipelineViewsFilterTransformation(dbMirror.value))
    }

    private fun pipelineViewsFilterTransformation(it: List<ServerWithPipelineViews>?): MailPackage<List<ServerWithPipelineViews>> {
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
        pipelineViewDao.deleteAndInsertPipelineViews(list)
    }

    suspend fun refreshPipelineViews(either: Either<ServerInstance, List<ServerInstance>?>) {
        liveServersWithPipelineViews.postValue(MailPackage.loadingPackage())
        when (either) {
            is Either.Left -> downAndCachePipelineViews(either.value)
            is Either.Right -> downAndCachePipelineViews(either.value)
        }
    }

    private suspend fun downAndCachePipelineViews(serverList: List<ServerInstance>?) {
        val mail = downloadPipelineViews(serverList)
        if (mail.isOk) {
            mail.mailContent!!
            deleteAndInsertPipelineViews(mail.mailContent.flatMap { it.pipelineViewList })
        }
        if (mail.isError)
            liveServersWithPipelineViews.postValue(mail)
    }

    private suspend fun downAndCachePipelineViews(serverInstance: ServerInstance) {
        val mail = downloadPipelineViews(serverInstance)
        if (mail.isOk) {
            mail.mailContent!!
            deleteAndInsertPipelineViews(mail.mailContent.pipelineViewList)
        }
        if (mail.isError)
            liveServersWithPipelineViews.postValue(MailPackage.brokenPackage(mail.msg))
    }

    private suspend fun downloadPipelineViews(serverList: List<ServerInstance>?): MailPackage<List<ServerWithPipelineViews>> {
        if (serverList == null)
            return MailPackage.brokenPackage("server list is null")
        val list: MutableList<ServerWithPipelineViews> = mutableListOf()
        serverList.forEach {
            val mail = downloadPipelineViews(it)
            if (!mail.isOk)
                return MailPackage.brokenPackage("error while parsing pipelines from ${it.name}")
            mail.mailContent!!
            list.add(mail.mailContent)
        }
        return MailPackage(list)
    }

    private suspend fun downloadPipelineViews(serverInstance: ServerInstance): MailPackage<ServerWithPipelineViews> {
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
        val list = pipelineViewDao.selectDeleted()
        list.forEach {
            if (deletePipeline(it) != StatusCode.OK)
                insertPipelineView(it.apply {
                    deleted = false
                })
        }
    }

    suspend fun getPipelineRetrofit(pipelineView: PipelineView): Either<StatusCode, PipelineRetrofit> {
        val server = serverInstanceDao.findById(pipelineView.serverId)
            ?: return Either.Left(StatusCode.SERVER_ID_NOT_FOUND)
        return getPipelineRetrofit(server)
    }

    suspend fun getPipelineRetrofit(server: ServerInstance): Either<StatusCode, PipelineRetrofit> =
        try {
            Either.Right(PipelineRetrofit.getInstance(mixAddressWithPort(server.url)))
        } catch (e: IllegalArgumentException) {
            l("deletePipeline ${e.toString()}")
            Either.Left(StatusCode.NO_CONNECT)
        }

    suspend fun deletePipeline(pipelineView: PipelineView): StatusCode {
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
    }

    suspend fun findPipelineViewById(id: String): PipelineView? =
        pipelineViewDao.findPipelineViewById(id)

    suspend fun downloadPipelineString(pipelineView: PipelineView): Either<StatusCode, String> {
        val pipelineRetrofit = when (val res = getPipelineRetrofit(pipelineView)) {
            is Either.Left -> return Either.Left(res.value)
            is Either.Right -> res.value
        }
        val call = pipelineRetrofit.getPipeline(pipelineView.idNumber)
        val text =
            RetrofitHelper.getStringFromCall(call) ?: return Either.Left(StatusCode.NULL_RESPONSE)
        return Either.Right(text)
    }

    companion object {
        private val TAG = Injector.tag(this)
        private fun l(msg: String) = Log.d(TAG, msg)
        private const val FRONTEND_PORT: Short = 8080
        private fun mixAddressWithPort(address: String) = "${address}:${FRONTEND_PORT}/"
    }
}
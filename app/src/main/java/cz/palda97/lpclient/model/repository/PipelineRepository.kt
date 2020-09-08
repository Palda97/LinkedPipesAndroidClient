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
import kotlinx.coroutines.delay
import java.io.IOException

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
        //val pipelineRetrofit = PipelineRetrofit.getInstance("${serverInstance.url}:8080/")
        val pipelineRetrofit = try {
            PipelineRetrofit.getInstance("${serverInstance.url}$FRONTEND_PORT")
        } catch (e: IllegalArgumentException) {
            l("downloadPipelineViews ${e.toString()}")
            return MailPackage.brokenPackage(e.toString())
        }
        val call = pipelineRetrofit.pipelineList()
        val text: String? = try {
            val response = call.execute().body()
            //response?.string() ?: "There is no ResponseBody"
            response?.string().also {
                l("downloadPipelineViews ${it.toString()}")
            }
        } catch (e: IOException) {
            l("downloadPipelineViews ${e.toString()}")
            null
        }
        return PipelineViewFactory(
            serverInstance,
            text
        ).serverWithPipelineViews
    }

    enum class DeleteCode {
        SERVER_ID_NOT_FOUND, NO_CONNECT, PIPELINE_NOT_FOUND, OK, INTERNAL_ERROR
    }

    suspend fun cleanDb() {
        val list = pipelineViewDao.selectDeleted()
        list.forEach {
            if (deletePipeline(it) != DeleteCode.OK)
                insertPipelineView(it.apply {
                    deleted = false
                })
        }
    }

    suspend fun deletePipeline(pipelineView: PipelineView): DeleteCode {
        val server = serverInstanceDao.findById(pipelineView.serverId)
            ?: return DeleteCode.SERVER_ID_NOT_FOUND
        val pipelineRetrofit = try {
            PipelineRetrofit.getInstance("${server.url}$FRONTEND_PORT")
        } catch (e: IllegalArgumentException) {
            l("deletePipeline ${e.toString()}")
            return DeleteCode.NO_CONNECT
        }
        val call = pipelineRetrofit.deletePipeline(pipelineView.id.split("/").last())
        val text: String? = try {
            val response = call.execute().body()
            //response?.string() ?: "There is no ResponseBody"
            response?.string()
        } catch (e: IOException) {
            l("deletePipeline ${e.toString()}")
            null
        }
        if (text == null) { //Pipeline was already deleted
            l("deletePipeline text is null")
            deleteRoutine(pipelineView)
            return DeleteCode.PIPELINE_NOT_FOUND
        }
        l(text)
        if (text.isEmpty()) {//Deletion was successful
            l("text isEmpty")
            deleteRoutine(pipelineView)
            return DeleteCode.OK
        }
        return DeleteCode.INTERNAL_ERROR
    }

    private suspend fun deleteRoutine(pipelineView: PipelineView) {
        pipelineViewDao.deletePipelineView(pipelineView)
    }

    suspend fun findPipelineViewById(id: String): PipelineView? =
        pipelineViewDao.findPipelineViewById(id)

    companion object {
        private val TAG = Injector.tag(this)
        private fun l(msg: String) = Log.d(TAG, msg)
        private const val FRONTEND_PORT = ":8080/"
    }
}
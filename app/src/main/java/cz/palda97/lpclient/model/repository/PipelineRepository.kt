package cz.palda97.lpclient.model.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.liveData
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.*
import cz.palda97.lpclient.model.db.dao.PipelineViewDao
import cz.palda97.lpclient.model.db.dao.ServerInstanceDao
import cz.palda97.lpclient.model.network.PipelineRetrofit
import java.io.IOException

class PipelineRepository(
    private val pipelineViewDao: PipelineViewDao,
    private val serverInstanceDao: ServerInstanceDao
) {

    /*fun idk() {
        /*val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.2.52:8080/")
            .build()
        val pipelineRetrofit = retrofit.create(PipelineRetrofit::class.java)*/
        val pipelineRetrofit = PipelineRetrofit.getInstance("http://192.168.1.52:8080/")
        val call = pipelineRetrofit.pipelineList()
        val text = try {
            val response = call.execute().body()
            response?.string() ?: "null xd"
        } catch (e: IOException) {
            e.toString()
        }
        l(text)
    }

    fun downloadPipelines(serverList: List<ServerInstance>) = serverList.forEach(::downloadPipelines)
    fun downloadPipelines(serverInstance: ServerInstance) {
        val pipelineRetrofit = PipelineRetrofit.getInstance(serverInstance.url)
        val call = pipelineRetrofit.pipelineList()
        val text = try {
            val response = call.execute().body()
            response?.string() ?: "There is no ResponseBody"
        } catch (e: IOException) {
            e.toString()
        }
        l(text)
    }*/

    //------------------------------------------------------------------------------------------------------------------------
    private val dbMirror = serverInstanceDao.serverListWithPipelineViews()

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
    //------------------------------------------------------------------------------------------------------------------------

    suspend fun insertPipelineViews(list: List<PipelineView>) {
        pipelineViewDao.insertList(list)
    }

    suspend fun downAndCachePipelineViews(serverList: List<ServerInstance>?) {
        val mail = downloadPipelineViews(serverList)
        if (mail.isOk) {
            mail.mailContent!!
            insertPipelineViews(mail.mailContent.flatMap { it.pipelineViewList })
        }
    }

    suspend fun downAndCachePipelineViews(serverInstance: ServerInstance) {
        val mail = downloadPipelineViews(serverInstance)
        if (mail.isOk) {
            mail.mailContent!!
            insertPipelineViews(mail.mailContent.pipelineViewList)
        }
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
            PipelineRetrofit.getInstance("${serverInstance.url}:8080/")
        } catch (e: IllegalArgumentException) {
            l(e.toString())
            return MailPackage.brokenPackage(e.toString())
        }
        val call = pipelineRetrofit.pipelineList()
        val text: String? = try {
            val response = call.execute().body()
            //response?.string() ?: "There is no ResponseBody"
            response?.string().also {
                l(it.toString())
            }
        } catch (e: IOException) {
            l(e.toString())
            null
        }
        return PipelineViewFactory(
            serverInstance,
            text
        ).serverWithPipelineViews
    }

    suspend fun deletePipeline(pipelineView: PipelineView) {
        /*val pipelineRetrofit = try {
            PipelineRetrofit.getInstance("${pipelineView.server.url}:8080/")
        } catch (e: IllegalArgumentException) {
            l(e.toString())
            return
        }
        val call = pipelineRetrofit.deletePipeline(pipelineView.id.split("/").last())
        val text: String? = try {
            val response = call.execute().body()
            //response?.string() ?: "There is no ResponseBody"
            response?.string().also {
                l(it.toString())
            }
        } catch (e: IOException) {
            l(e.toString())
            null
        }
        //
        */
    }

    companion object {
        private val TAG = Injector.tag(this)
        private fun l(msg: String) = Log.d(TAG, msg)
    }
}
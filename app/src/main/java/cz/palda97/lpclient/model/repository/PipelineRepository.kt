package cz.palda97.lpclient.model.repository

import android.util.Log
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.ServerInstance
import cz.palda97.lpclient.model.network.PipelineRetrofit
import cz.palda97.lpclient.viewmodel.pipelines.PipelineView
import cz.palda97.lpclient.viewmodel.pipelines.PipelineViewFactory
import retrofit2.Retrofit
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException

class PipelineRepository {

    fun idk() {
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

    /*fun downloadPipelines(serverList: List<ServerInstance>) = serverList.forEach(::downloadPipelines)
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

    suspend fun downloadPipelineViews(serverList: List<ServerInstance>?): MailPackage<List<PipelineView>> {
        if (serverList == null)
            return MailPackage.brokenPackage()
        var list: List<PipelineView> = emptyList()
        serverList.forEach {
            val mail = downloadPipelineViews(it)
            if (!mail.isOk)
                return MailPackage.brokenPackage()
            mail.mailContent!!
            list = list + mail.mailContent
        }
        return MailPackage(list)
    }

    suspend fun downloadPipelineViews(serverInstance: ServerInstance): MailPackage<List<PipelineView>> {
        //val pipelineRetrofit = PipelineRetrofit.getInstance("${serverInstance.url}:8080/")
        val pipelineRetrofit = try {
            PipelineRetrofit.getInstance("${serverInstance.url}:8080/")
        }catch (e: IllegalArgumentException){
            l(e.toString())
            return MailPackage.brokenPackage()
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
        val list = PipelineViewFactory(serverInstance, text).pipelineList
        return if (list == null)
            MailPackage.brokenPackage()
        else
            MailPackage(list)
    }

    companion object {
        //private const val TAG = "PipelineRepository"
        //private val TAG = this::class.java.declaringClass?.canonicalName.toString().split(".").reversed()[0]
        private val TAG = Injector.tag(this)
        private fun l(msg: String) = Log.d(TAG, msg)
        /*
        private var l: String
            get() = ""
            set(value) {
                Log.d(TAG, value)
            }
        */
    }
}
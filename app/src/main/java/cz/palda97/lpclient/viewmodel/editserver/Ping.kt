package cz.palda97.lpclient.viewmodel.editserver

import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.entities.server.ServerInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException

//class Ping(private val apiUrl: String) {
class Ping(server: ServerInstance) {

    val pingUrl = address(server.url)
    val apiUrl = server.frontendUrl

    enum class Status {
        OK, NO, UNKNOWN_HOST, SECURITY, IO, API_OK
    }

    suspend fun tryApiCall(): Status = withContext(Dispatchers.IO) {
        val pipelineRepository = Injector.pipelineRepository
        val mail = pipelineRepository.downloadPipelineViews(ServerInstance(url = apiUrl))
        if (mail.isOk)
            Status.API_OK
        else
            Status.NO
    }

    suspend fun ping(): Status = withContext(Dispatchers.IO) {
        if (pingUrl.isEmpty())
            return@withContext Status.UNKNOWN_HOST
        try {
            val ip = InetAddress.getByName(pingUrl)
            if (ip?.isReachable(TIMEOUT) == true)
                Status.OK
            else
                Status.NO
        } catch (e: UnknownHostException) {
            Status.UNKNOWN_HOST
        } catch (e: SecurityException) {
            Status.SECURITY
        } catch (e: IOException) {
            Status.IO
        }
    }

    companion object {
        private const val TIMEOUT: Int = 2000

        private fun address(url: String): String = url
            .removePrefix("http://")
            .removePrefix("https://")
            .split("/").first()
    }
}
package cz.palda97.lpclient.model.entities.server

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.net.MalformedURLException
import java.net.URL

/**
 * Factory for transforming json to [ServerInstance].
 */
object ServerFactory {

    /**
     * Parse [json] to [ServerInstance].
     * @return ServerInstance or null on error.
     */
    private fun fromJson(json: String?): ServerInstance? = try {
        Gson().fromJson(json, ServerInstance::class.java)
    } catch (e: JsonSyntaxException) {
        null
    }

    private val String.isValidUrl: Boolean
        get() {
            //return Patterns.WEB_URL.matcher(this).matches()
            //return URLUtil.isNetworkUrl(this)
            return startsWith("http://", true) || startsWith("https://", true)
        }

    private val String.serverURL: Pair<String, Int?>?
        get() {
            val url = try { URL(this) }
            catch (_: MalformedURLException) { return null }
            val port = if (url.port == -1 || url.port == url.defaultPort) null else url.port
            return "${url.protocol}://${url.host}${url.path}" to port
        }

    private fun fromURL(url: String?): ServerInstance? {
        if (url == null) return null
        if (!url.isValidUrl) {
            return null
        }
        val (address, port) = url.serverURL ?: return null
        val server = ServerInstance(url = address)
        port?.let { server.frontend = it }
        return server
    }

    fun fromString(text: String?): ServerInstance? {
        fromJson(text)?.let { return it }
        return fromURL(text)
    }
}
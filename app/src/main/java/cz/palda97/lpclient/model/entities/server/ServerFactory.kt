package cz.palda97.lpclient.model.entities.server

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

/**
 * Factory for transforming json to [ServerInstance].
 */
object ServerFactory {

    /**
     * Parse [json] to [ServerInstance].
     * @return ServerInstance or null on error.
     */
    fun fromJson(json: String?): ServerInstance? = try {
        Gson().fromJson(json, ServerInstance::class.java)
    } catch (e: JsonSyntaxException) {
        null
    }
}
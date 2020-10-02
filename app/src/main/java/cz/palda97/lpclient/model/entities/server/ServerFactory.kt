package cz.palda97.lpclient.model.entities.server

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

object ServerFactory {
    fun fromJson(json: String?): ServerInstance? = try {
        Gson().fromJson(json, ServerInstance::class.java)
    } catch (e: JsonSyntaxException) {
        null
    }
}
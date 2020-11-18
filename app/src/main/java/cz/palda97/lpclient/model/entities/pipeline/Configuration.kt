package cz.palda97.lpclient.model.entities.pipeline

import cz.palda97.lpclient.model.travelobjects.CommonFunctions
import cz.palda97.lpclient.model.travelobjects.LdConstants

//data class Configuration(val settings: Map<*, *>, val type: String, val id: String)
data class Configuration(val settings: List<Config>, val id: String) {
    fun getString(key: String): String? {
        settings.forEach {
            val value = it.getString(key)
            if (value != null) {
                return value
            }
        }
        return null
    }
}
data class Config(val settings: MutableMap<*, *>, val type: String, val id: String) {
    fun getString(key: String) = CommonFunctions.giveMeThatConfigString(settings, key, LdConstants.VALUE)
    /*fun getBoolean(key: String): Boolean {
        val string = getString(key) ?: ""
        return string.toBoolean()
    }*/
}
package cz.palda97.lpclient.model.entities.pipeline

import androidx.room.Entity
import androidx.room.PrimaryKey
import cz.palda97.lpclient.model.travelobjects.CommonFunctions
import cz.palda97.lpclient.model.travelobjects.LdConstants

@Entity
data class Configuration(val settings: List<Config>, @PrimaryKey(autoGenerate = false) val id: String) {

    private fun getMainConfig(configType: String) = settings.find { it.type == configType }

    fun getString(key: String, configType: String): String? {
        val config = getMainConfig(configType) ?: return null
        return config.getString(key)
    }

    fun setString(key: String, newValue: String, configType: String) {
        val config = getMainConfig(configType) ?: return
        config.setString(key, newValue)
        if (config.id.contains("/new/")) {
            config.id = id
        }
    }

    fun getInheritances(regex: Regex, configType: String): List<Pair<String, Boolean>>? {
        val config = getMainConfig(configType) ?: return null
        return config.getControlsAndIds(regex)
    }
}

data class Config(val settings: MutableMap<*, *>, val type: String, var id: String) {
    fun getString(key: String) = CommonFunctions.giveMeThatString(settings, key, LdConstants.VALUE)
    fun setString(key: String, value: String) {
        CommonFunctions.saveMeThatString(settings, key, LdConstants.VALUE, value)
    }
    fun getControlsAndIds(regex: Regex): List<Pair<String, Boolean>>? {
        val controlMap = settings.filterKeys {
            val key = it as? String ?: return null
            key matches regex
        }
        return controlMap.map {
            val key = it.key as? String ?: return null
            val innerList = it.value as? List<*> ?: return null
            val innerMap = innerList.firstOrNull() as? Map<*, *> ?: return null
            val inheritanceString = innerMap[LdConstants.ID] as? String ?: return null
            val inheritance = when(inheritanceString) {
                LdConstants.INHERITANCE_NONE -> false
                LdConstants.INHERITANCE_INHERIT -> true
                else -> return null
            }
            key to inheritance
        }
    }
}
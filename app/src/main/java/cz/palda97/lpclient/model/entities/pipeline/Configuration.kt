package cz.palda97.lpclient.model.entities.pipeline

import androidx.room.Entity
import androidx.room.PrimaryKey
import cz.palda97.lpclient.model.travelobjects.CommonFunctions
import cz.palda97.lpclient.model.travelobjects.LdConstants

@Entity
data class Configuration(val settings: List<Config>, @PrimaryKey(autoGenerate = false) val id: String) {

    fun getString(key: String, configType: String): String? {
        val config = settings.find { it.type == configType } ?: return null
        return config.getString(key)
    }

    fun setString(key: String, newValue: String, configType: String) {
        val config = settings.find { it.type == configType } ?: return
        config.setString(key, newValue)
        if (config.id.contains("/new/")) {
            config.id = id
        }
    }
}

data class Config(val settings: MutableMap<*, *>, val type: String, var id: String) {
    fun getString(key: String) = CommonFunctions.giveMeThatString(settings, key, LdConstants.VALUE)
    fun setString(key: String, value: String) {
        CommonFunctions.saveMeThatString(settings, key, LdConstants.VALUE, value)
    }
}
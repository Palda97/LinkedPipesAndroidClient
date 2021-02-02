package cz.palda97.lpclient.model.entities.pipeline

import androidx.room.Entity
import androidx.room.PrimaryKey
import cz.palda97.lpclient.model.travelobjects.CommonFunctions
import cz.palda97.lpclient.model.travelobjects.LdConstants

@Entity
data class Configuration(val settings: List<Config>, @PrimaryKey(autoGenerate = false) val id: String) {
    fun getString(key: String): String? {
        settings.forEach {
            val value = it.getString(key)
            if (value != null) {
                return value
            }
        }
        return null
    }
    fun setString(key: String, newValue: String) {
        settings.forEach {
            val value = it.getString(key)
            if (value != null) {
                it.setString(key, newValue)
                return
            }
        }
    }
}

data class Config(val settings: MutableMap<*, *>, val type: String, val id: String) {
    fun getString(key: String) = CommonFunctions.giveMeThatString(settings, key, LdConstants.VALUE)
    fun setString(key: String, value: String) {
        CommonFunctions.saveMeThatString(settings, key, LdConstants.VALUE, value)
    }
}
package cz.palda97.lpclient.model.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import cz.palda97.lpclient.model.entities.execution.ExecutionStatus
import cz.palda97.lpclient.model.entities.pipeline.Binding
import cz.palda97.lpclient.model.entities.pipeline.Config
import cz.palda97.lpclient.model.entities.pipeline.ConfigInput
import java.util.*

/**
 * Functions that Room can use to work with entities that have non primitive members.
 */
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }

    @TypeConverter
    fun fromExecutionStatus(executionStatus: ExecutionStatus?): Int? {
        return executionStatus?.let {
            when (it) {
                ExecutionStatus.FAILED -> 0
                ExecutionStatus.FINISHED -> 1
                ExecutionStatus.RUNNING -> 2
                ExecutionStatus.CANCELLED -> 3
                ExecutionStatus.DANGLING -> 4
                ExecutionStatus.CANCELLING -> 5
                ExecutionStatus.QUEUED -> 6
                ExecutionStatus.MAPPED -> 7
            }
        }
    }

    @TypeConverter
    fun toExecutionStatus(value: Int?): ExecutionStatus? {
        return value?.let {
            when (it) {
                0 -> ExecutionStatus.FAILED
                1 -> ExecutionStatus.FINISHED
                2 -> ExecutionStatus.RUNNING
                3 -> ExecutionStatus.CANCELLED
                4 -> ExecutionStatus.DANGLING
                5 -> ExecutionStatus.CANCELLING
                6 -> ExecutionStatus.QUEUED
                7 -> ExecutionStatus.MAPPED
                else -> null
            }
        }
    }

    @TypeConverter
    fun fromBindingType(type: Binding.Type?): Int? {
        return when(type) {
            Binding.Type.CONFIGURATION -> 0
            Binding.Type.INPUT -> 1
            Binding.Type.OUTPUT -> 2
            null -> null
        }
    }

    @TypeConverter
    fun toBindingType(value: Int?): Binding.Type? {
        return when(value) {
            0 -> Binding.Type.CONFIGURATION
            1 -> Binding.Type.INPUT
            2 -> Binding.Type.OUTPUT
            else -> null
        }
    }

    @TypeConverter
    fun fromMap(map: Map<*, *>?): String? {
        return Gson().toJson(map)
    }

    /*@TypeConverter
    fun tpMap(string: String?): Map<*, *>? {
        return try {
            Gson().fromJson(string, Map::class.java)
        } catch (e: JsonSyntaxException) {
            null
        }
    }*/

    private inline fun <reified R: Any> defaultConverter(it: Any?): R? = it as? R
    private inline fun <reified R: Any> fromJsonToList(string: String?, convertFun: (Any?) -> R? = ::defaultConverter): List<R>? {
        return try {
            val jsonObject = Gson().fromJson(string, List::class.java)
            jsonObject?.map {
                convertFun(it) ?: return null
            }
        } catch (e: JsonSyntaxException) {
            null
        }
    }

    @TypeConverter
    fun fromListConfig(list: List<Config>?): String? {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toListConfig(string: String?): List<Config>? {
        return fromJsonToList(string) {
            val map = it as? Map<*, *> ?: return null
            val id: String = map["id"] as? String ?: return null
            val settings: Map<*, *> = map["settings"] as? Map<*, *> ?: return null
            val type: String = map["type"] as? String ?: return null
            Config(settings.toMutableMap(), type, id)
        }
    }

    @TypeConverter
    fun fromListString(list: List<String>?): String? {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toListString(string: String?): List<String>? {
        return fromJsonToList(string)
    }

    @TypeConverter
    fun fromListOfPairs(list: List<Pair<String, String>>?): String? {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toListOfPairs(string: String?): List<Pair<String, String>>? {
        return fromJsonToList(string) {
            val map = it as? Map<*, *> ?: return null
            val first: String = map["first"] as? String ?: return null
            val second: String = map["second"] as? String ?: return null
            first to second
        }
    }

    @TypeConverter
    fun fromConfigInputType(type: ConfigInput.Type?): Int? {
        return when(type) {
            ConfigInput.Type.EDIT_TEXT -> 0
            ConfigInput.Type.SWITCH -> 1
            ConfigInput.Type.DROPDOWN -> 2
            ConfigInput.Type.TEXT_AREA -> 3
            null -> null
        }
    }

    @TypeConverter
    fun toConfigInputType(value: Int?): ConfigInput.Type? {
        return when(value) {
            0 -> ConfigInput.Type.EDIT_TEXT
            1 -> ConfigInput.Type.SWITCH
            2 -> ConfigInput.Type.DROPDOWN
            3 -> ConfigInput.Type.TEXT_AREA
            else -> null
        }
    }

    @TypeConverter
    fun toMapStringString(string: String?): Map<String, String>? {
        return try {
            val jsonObject = Gson().fromJson(string, Map::class.java)
            val list = jsonObject.map {
                val key = it.key as? String ?: return null
                val value = it.value as? String ?: return null
                key to value
            }
            mapOf(
                *(list.toTypedArray())
            )
        } catch (e: JsonSyntaxException) {
            null
        }
    }
}
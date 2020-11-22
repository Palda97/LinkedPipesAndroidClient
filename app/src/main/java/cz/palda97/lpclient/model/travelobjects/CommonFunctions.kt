package cz.palda97.lpclient.model.travelobjects

import com.google.gson.Gson
import cz.palda97.lpclient.model.*
import cz.palda97.lpclient.model.travelobjects.LdConstants.GRAPH
import cz.palda97.lpclient.model.travelobjects.LdConstants.ID
import cz.palda97.lpclient.model.travelobjects.LdConstants.TYPE

object CommonFunctions {
    fun prepareSemiRootElement(rootElement: Any?): ArrayList<*>? {
        if (rootElement !is Map<*, *>)
            return null
        return (rootElement[GRAPH] ?: return null) as? ArrayList<*> ?: return null
    }

    fun giveMeThatString(map: Map<*, *>, key1: String, key2: String): String? {
        val list = (map[key1] ?: return null) as? ArrayList<*> ?: return null
        val innerMap = (list[0] ?: return null) as? Map<*, *> ?: return null
        return innerMap[key2] as? String ?: return null
    }

    /*fun giveMeThatConfigString(map: Map<*, *>, key1: String, key2: String): String? {
        val updatedKeys = map.mapKeys {
            val key = it.key as? String ?: return null
            key.replaceBefore("#", "").replaceFirst("#", "")
        }
        //println("+++++++++++++++++++++++++++++++++++ ${updatedKeys.keys}")
        return giveMeThatString(updatedKeys, key1, key2)
    }*/

    fun giveMeThatId(map: Any?): String? {
        if (map !is Map<*, *>)
            return null
        return (map[ID] ?: return null) as? String ?: return null
    }

    fun giveMeThatType(map: Map<*, *>): String? {
        val list = (map[TYPE] ?: return null) as? ArrayList<*> ?: return null
        if (list.size != 1)
            return null
        return list[0] as? String ?: return null
    }

    fun giveMeThoseTypes(map: Map<*, *>): List<String>? {
        val list = (map[TYPE] ?: return null) as? ArrayList<*> ?: return null
        return list.map {
            it as? String ?: return null
        }
    }

    fun getRootArrayList(string: String?): Either<String, ArrayList<*>> {
        if (string == null)
            return Either.Left("string is null")
        val gsonObject =
            Gson().fromJson(string, Any::class.java) ?: return Either.Left("null pointer")
        return when (gsonObject) {
            is ArrayList<*> -> Either.Right(gsonObject)
            else -> Either.Left("root element not arraylist")
        }
    }
}
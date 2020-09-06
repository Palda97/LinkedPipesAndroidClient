package cz.palda97.lpclient.model.travelobjects

import cz.palda97.lpclient.model.travelobjects.LdConstants.GRAPH
import cz.palda97.lpclient.model.travelobjects.LdConstants.ID
import cz.palda97.lpclient.model.travelobjects.LdConstants.TYPE

object CommonFunctions {
    fun prepareSemiRootElement(rootElement: Any?): ArrayList<*>? {
        if(rootElement !is Map<*, *>)
            return null
        return (rootElement[GRAPH] ?: return null) as? ArrayList<*> ?: return null
    }
    fun giveMeThatString(map: Map<*,*>, key1: String, key2: String): String? {
        val list = (map[key1] ?: return null) as? ArrayList<*> ?: return null
        val innerMap = (list[0] ?: return null) as? Map<*,*> ?: return null
        return innerMap[key2] as? String ?: return null
    }
    fun giveMeThatId(map: Map<*,*>): String? {
        return (map[ID] ?: return null) as? String ?: return null
    }
    fun giveMeThatType(map: Map<*,*>): String? {
        val list = (map[TYPE] ?: return null) as? ArrayList<*> ?: return null
        if(list.size != 1)
            return null
        return list[0] as? String ?: return null
    }
}
package cz.palda97.lpclient.model.entities.pipeline

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

class DialogJsFactory(private val js: String, private val componentId: String) {

    fun parse(): DialogJs? {
        if (js.isEmpty()) {
            return DialogJs("", mapOf(), componentId)
        }
        val json = js
            .substringAfter(CONST_DESC)
            .replaceAfter(JSON_END, "")
            .replace(",\\s*\\};".toRegex(), "\n};")
            .removeSuffix(SEMICOLON)
        val jsonObject = try {
            Gson().fromJson(json, Any::class.java) as? Map<*, *>
        } catch (e: JsonSyntaxException) {
            null
        } ?: return null
        val namespace = jsonObject[NAMESPACE] as? String ?: return null
        val map: MutableMap<String, String> = HashMap()
        jsonObject.forEach {
            if (it.key is String) {
                val key = (it.key as? String) ?: return null
                val skip = key.startsWith(PREFIX)
                if (!skip) {
                    val innerMap = it.value as? Map<*, *>
                    val prop = innerMap?.get(PROPERTY) as? String
                    if (prop == null) {
                        map[key] = key
                    } else {
                        map[key] = prop
                    }
                }
            }
        }
        return DialogJs(namespace, map, componentId)
    }

    companion object {
        private const val CONST_DESC = "const DESC = "
        private const val PREFIX = "$"
        private const val JSON_END = "};"
        private const val SEMICOLON = ";"
        private const val PROPERTY = "\$property"
        private const val NAMESPACE = "\$namespace"
    }
}
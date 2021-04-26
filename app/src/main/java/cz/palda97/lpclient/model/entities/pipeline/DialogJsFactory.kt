package cz.palda97.lpclient.model.entities.pipeline

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import cz.palda97.lpclient.Injector

/**
 * Factory for transforming dialog.js to [DialogJs].
 */
class DialogJsFactory(private val js: String, private val componentId: String) {

    /**
     * Parse javascript to [DialogJs].
     * @return DialogJs or null on error.
     */
    fun parse(): DialogJs? {
        if (js.isEmpty()) {
            return DialogJs("", mapOf(), componentId)
        }
        val json = js
            .substringAfter(CONST_DESC)
            .replaceAfter(JSON_END, "")
            .replace(",\\s*\\};".toRegex(), "\n};")
            .removeSuffix(SEMICOLON)
            .removeAnonFunctions()
        val jsonObject = try {
            Gson().fromJson(json, Any::class.java) as? Map<*, *>
        } catch (e: JsonSyntaxException) {
            null
        } ?: return null.also {
            l(0)
            l(json)
        }
        val namespace = jsonObject[NAMESPACE] as? String ?: return null.also {
            l(1)
        }
        val map: MutableMap<String, String> = HashMap()
        jsonObject.forEach {
            if (it.key is String) {
                val key = (it.key as? String) ?: return null.also {
                    l(2)
                }
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

        private val l = Injector.generateLogFunction(this)

        /**
         * Removes anonymous functions like this:
         *
         * &nbsp;
         *
         * "$onLoad": (value) => {
         *
         * &nbsp;&nbsp;&nbsp;&nbsp;return value.join(",");
         *
         * }
         */
        fun String.removeAnonFunctions(): String {

            val r = "\\n\\s*\".+\"\\s*:\\s*\\(.+\\)\\s*=>\\s*\\{"
            val r1 = ",\\s*$r".toRegex()
            val r2 = r.toRegex()

            tailrec fun loop(comma: Boolean, json: String): String {

                val regex = if (comma) r1 else r2

                fun findEnd(json: String): Int? {
                    var openCnt = 0
                    json.forEachIndexed { i, c ->
                        when(c) {
                            '{' -> openCnt += 1
                            '}' -> openCnt -= 1
                        }
                        if (openCnt == 0) {
                            return i
                        }
                    }
                    return null
                }

                val match = regex.find(json) ?: return json
                val start = match.range.first
                val tmpEnd = findEnd(json.removeRange(0, match.range.last)) ?: return json
                val tmpEnd2 = tmpEnd + match.range.last + 1
                val end = if (!comma && json[tmpEnd2] == ',') {
                    tmpEnd2 + 1
                } else {
                    tmpEnd2
                }
                val newJson = json.removeRange(start, end)
                return loop(comma, newJson)
            }

            val firstStage = loop(true, this)
            return loop(false, firstStage)
        }
    }
}
package cz.palda97.lpclient.model.entities.pipeline

data class DialogJs(val namespace: String, val map: Map<String, String>) {
    fun getFullPropertyName(key: String): String? {
        val string = map[key] ?: return null
        return "$namespace$string"
    }
}
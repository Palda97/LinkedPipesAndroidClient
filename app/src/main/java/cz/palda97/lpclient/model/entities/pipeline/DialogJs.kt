package cz.palda97.lpclient.model.entities.pipeline

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
data class DialogJs(
    val namespace: String,
    val map: Map<String, String>,
    @PrimaryKey(autoGenerate = false) val componentId: String
) {

    fun getFullPropertyName(key: String): String? {
        val string = map[key] ?: return null
        return "$namespace$string"
    }

    @Ignore
    val configType = "${namespace}Configuration"

    @Ignore
    val controlRegex = "${Regex.escape(namespace)}.+$CONTROL".toRegex()

    @Ignore
    private val reversedMap = map.entries.associate { it.value to it.key }

    private fun getReversedName(fullName: String): String {
        val key = fullName.removePrefix(namespace)
        return reversedMap[key] ?: key
    }

    fun fullControlNameToReverse(controlName: String) =
        getReversedName(controlName.removeSuffix(CONTROL))

    companion object {
        private const val CONTROL = "Control"
    }
}
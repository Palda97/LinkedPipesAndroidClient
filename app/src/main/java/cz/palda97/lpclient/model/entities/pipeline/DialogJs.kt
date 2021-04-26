package cz.palda97.lpclient.model.entities.pipeline

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * Entity representing dialog.js.
 * It is mainly used as a tool for mapping
 * [ConfigInput's][ConfigInput] id to [Config's][Config] id.
 */
@Entity
data class DialogJs(
    val namespace: String,
    val map: Map<String, String>,
    @PrimaryKey(autoGenerate = false) val componentId: String
) {

    /**
     * Maps [ConfigInput's][ConfigInput] id to [Config's][Config] id.
     * @param key ConfigInput's id.
     * @return Full Config's id or null if id not found.
     */
    fun getFullPropertyName(key: String): String? {
        val string = map[key] ?: return null
        return "$namespace$string"
    }

    /**
     * The main [Config].
     */
    @Ignore
    val configType = "${namespace}Configuration"

    /**
     * Regex for matching the inheritance fields.
     */
    @Ignore
    val controlRegex = "${Regex.escape(namespace)}.+$CONTROL".toRegex()

    @Ignore
    private val reversedMap = map.entries.associate { it.value to it.key }

    /**
     * Maps [Config's][Config] id to [ConfigInput's][ConfigInput] id.
     * @param fullName Full Config's id.
     * @return ConfigInput's id.
     */
    private fun getReversedName(fullName: String): String {
        val key = fullName.removePrefix(namespace)
        return reversedMap[key] ?: key
    }

    /**
     * Maps inheritance id to [ConfigInput's][ConfigInput] id.
     * @param fullName Full Config's id.
     * @return ConfigInput's id.
     */
    fun fullControlNameToReverse(controlName: String) =
        getReversedName(controlName.removeSuffix(CONTROL))

    companion object {
        private const val CONTROL = "Control"
    }
}
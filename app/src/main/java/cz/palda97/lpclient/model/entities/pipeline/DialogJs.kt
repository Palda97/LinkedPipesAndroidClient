package cz.palda97.lpclient.model.entities.pipeline

import androidx.room.Entity
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
}
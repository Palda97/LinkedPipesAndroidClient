package cz.palda97.lpclient.model.entities.pipeline

import androidx.room.Entity

@Entity(primaryKeys = ["id", "componentId"])
data class ConfigInput(
    val label: String,
    val type: Type,
    val id: String,
    val componentId: String,
    val options: List<Pair<String, String>> = listOf()
) {
    enum class Type {
        EDIT_TEXT, SWITCH, DROPDOWN, TEXT_AREA
    }
}
package cz.palda97.lpclient.model.entities.pipeline

data class ConfigInput(
    val label: String,
    val type: Type,
    val id: String,
    val options: MutableList<Pair<String, String>> = mutableListOf()
) {
    enum class Type {
        EDIT_TEXT, SWITCH, DROPDOWN
    }
}
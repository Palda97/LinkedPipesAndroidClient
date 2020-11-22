package cz.palda97.lpclient.model.entities.pipeline

data class Binding(
    val componentId: String,
    val type: Type,
    val bindingValue: String,
    val prefLabel: String,
    val id: String
) {
    enum class Type {
        CONFIGURATION, INPUT, OUTPUT
    }
}
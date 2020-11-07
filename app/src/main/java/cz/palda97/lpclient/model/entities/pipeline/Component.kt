package cz.palda97.lpclient.model.entities.pipeline

data class Component(
    val configurationId: String,
    val templateId: String,
    var x: Int,
    var y: Int,
    val prefLabel: String,
    val description: String?,
    val id: String
)
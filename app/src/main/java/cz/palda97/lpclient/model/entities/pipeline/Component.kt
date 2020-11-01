package cz.palda97.lpclient.model.entities.pipeline

data class Component(
    val configurationId: String,
    val templateId: String,
    val x: Int,
    val y: Int,
    val prefLabel: String,
    val description: String?,
    val id: String
)
package cz.palda97.lpclient.model.entities.pipeline

data class Template(
    val configurationId: String,
    val templateId: String,
    val prefLabel: String,
    val description: String?,
    val id: String
)
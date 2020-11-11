package cz.palda97.lpclient.model.entities.pipeline

data class Component(
    val configurationId: String,
    val templateId: String,
    var x: Int,
    var y: Int,
    val prefLabel: String,
    val description: String?,
    val id: String
) {
    constructor(
        x: Int,
        y: Int,
        template: Template
    ) : this(
        template.configurationId,
        template.templateId,
        x,
        y,
        template.prefLabel,
        template.description,
        template.id
    )
}
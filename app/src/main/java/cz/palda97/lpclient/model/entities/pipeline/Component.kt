package cz.palda97.lpclient.model.entities.pipeline

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a component from a pipeline.
 */
@Entity
data class Component(
    var configurationId: String?,
    var templateId: String,
    var x: Int,
    var y: Int,
    var prefLabel: String,
    var description: String?,
    @PrimaryKey(autoGenerate = false) var id: String
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

/*data class MutableComponent(
    var configurationId: String,
    var templateId: String,
    var x: Int,
    var y: Int,
    var prefLabel: String,
    var description: String?,
    var id: String
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
    constructor(component: Component) : this(
        component.configurationId,
        component.templateId,
        component.x,
        component.y,
        component.prefLabel,
        component.description,
        component.id
    )
    fun toComponent() = Component(
        configurationId,
        templateId,
        x,
        y,
        prefLabel,
        description,
        id
    )
}*/
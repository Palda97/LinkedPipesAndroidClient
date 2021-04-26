package cz.palda97.lpclient.model.entities.possiblecomponent

import androidx.room.Entity

/**
 * Entity representing component that can be added to the pipeline.
 */
@Entity(primaryKeys = ["id", "serverId"])
data class PossibleComponent(
    val id: String,
    val serverId: Long,
    val prefLabel: String,
    val templateId: String?
)
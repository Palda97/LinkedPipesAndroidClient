package cz.palda97.lpclient.model.entities.possiblecomponent

import androidx.room.Entity

@Entity(primaryKeys = ["id", "serverId"])
data class PossibleComponent(
    val id: String,
    val serverId: Long,
    val prefLabel: String,
    val templateId: String?
)
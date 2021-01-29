package cz.palda97.lpclient.model.entities.possiblecomponent

import androidx.room.Embedded
import androidx.room.Relation

data class StatusWithPossibles (
    @Embedded val status: PossibleStatus,
    @Relation(
        parentColumn = "serverId",
        entityColumn = "serverId"
    )
    val list: List<PossibleComponent>
)
package cz.palda97.lpclient.model

import androidx.room.Embedded
import androidx.room.Relation

class ServerWithExecutions (
    @Embedded val server: ServerInstance,
    @Relation(
        parentColumn = "id",
        entityColumn = "serverId"
    )
    val executionList: List<Execution>
)
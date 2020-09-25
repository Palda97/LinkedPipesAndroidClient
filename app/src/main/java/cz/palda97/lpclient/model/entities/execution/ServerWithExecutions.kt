package cz.palda97.lpclient.model.entities.execution

import androidx.room.Embedded
import androidx.room.Relation
import cz.palda97.lpclient.model.entities.execution.Execution
import cz.palda97.lpclient.model.entities.server.ServerInstance

class ServerWithExecutions (
    @Embedded val server: ServerInstance,
    @Relation(
        parentColumn = "id",
        entityColumn = "serverId"
    )
    val executionList: List<Execution>
)
package cz.palda97.lpclient.model.entities.execution

import androidx.room.Embedded
import androidx.room.Relation
import cz.palda97.lpclient.model.entities.server.ServerInstance

data class ServerWithExecutions (
    @Embedded val server: ServerInstance,
    @Relation(
        entity = Execution::class,
        parentColumn = "id",
        entityColumn = "serverId"
    )
    val executionList: List<ExecutionWithDeleteStatus>
) {
    constructor(server: ServerInstance, executions: List<Execution>, dummy: Int = 0): this(server, executions.map { ExecutionWithDeleteStatus(it, null) })
}
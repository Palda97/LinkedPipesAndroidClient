package cz.palda97.lpclient.model.entities.execution

import androidx.room.Embedded
import androidx.room.Relation
import cz.palda97.lpclient.model.entities.server.ServerInstance

/**
 * [Server][ServerInstance] with [Executions][Execution] paired with corresponding
 * [marks][cz.palda97.lpclient.model.db.MarkForDeletion].
 */
data class ServerWithExecutions (
    @Embedded val server: ServerInstance,
    @Relation(
        entity = Execution::class,
        parentColumn = "id",
        entityColumn = "serverId"
    )
    val executionList: List<ExecutionWithDeleteStatus>
) {
    /**
     * Creates a [ServerWithExecutions] instance with null [marks][cz.palda97.lpclient.model.db.MarkForDeletion].
     */
    constructor(server: ServerInstance, executions: List<Execution>, dummy: Int = 0): this(server, executions.map { ExecutionWithDeleteStatus(it, null) })
}
package cz.palda97.lpclient.model.entities.pipelineview

import androidx.room.Embedded
import androidx.room.Relation
import cz.palda97.lpclient.model.entities.execution.Execution
import cz.palda97.lpclient.model.entities.server.ServerInstance

/**
 * [Server][ServerInstance] with [PipelineViews][PipelineView] paired with corresponding
 * [marks][cz.palda97.lpclient.model.db.MarkForDeletion].
 */
data class ServerWithPipelineViews(
    @Embedded val server: ServerInstance,
    @Relation(
        entity = PipelineView::class,
        parentColumn = "id",
        entityColumn = "serverId"
    )
    val pipelineViewList: List<PipelineViewWithDeleteStatus>
) {
    constructor(server: ServerInstance, pipelines: List<PipelineView>, dummy: Int = 0): this(server, pipelines.map { PipelineViewWithDeleteStatus(it, null) })
}
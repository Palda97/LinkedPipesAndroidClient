package cz.palda97.lpclient.model.entities.pipeline

import androidx.room.Embedded
import androidx.room.Relation
import cz.palda97.lpclient.model.entities.pipeline.PipelineView
import cz.palda97.lpclient.model.entities.server.ServerInstance

data class ServerWithPipelineViews(
    @Embedded val server: ServerInstance,
    @Relation(
        parentColumn = "id",
        entityColumn = "serverId"
    )
    val pipelineViewList: List<PipelineView>
)
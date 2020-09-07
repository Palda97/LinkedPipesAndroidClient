package cz.palda97.lpclient.model

import androidx.room.Embedded
import androidx.room.Relation

data class ServerWithPipelineViews(
    @Embedded val server: ServerInstance,
    @Relation(
        parentColumn = "id",
        entityColumn = "serverId"
    )
    val pipelineViewList: List<PipelineView>
)
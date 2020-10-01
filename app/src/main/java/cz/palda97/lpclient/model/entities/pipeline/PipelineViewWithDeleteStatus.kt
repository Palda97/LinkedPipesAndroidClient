package cz.palda97.lpclient.model.entities.pipeline

import androidx.room.Embedded
import androidx.room.Relation
import cz.palda97.lpclient.model.db.MarkForDeletion

data class PipelineViewWithDeleteStatus(
    @Embedded
    val pipelineView: PipelineView,
    @Relation(
        parentColumn = "id",
        entityColumn = "mark"
    )
    val mark: MarkForDeletion?
)
package cz.palda97.lpclient.model.entities.pipelineview

import androidx.room.Embedded
import androidx.room.Relation
import cz.palda97.lpclient.model.db.MarkForDeletion

/**
 * Wrapper for [PipelineView] and [MarkForDeletion].
 */
data class PipelineViewWithDeleteStatus(
    @Embedded
    val pipelineView: PipelineView,
    @Relation(
        parentColumn = "id",
        entityColumn = "mark"
    )
    val mark: MarkForDeletion?
)
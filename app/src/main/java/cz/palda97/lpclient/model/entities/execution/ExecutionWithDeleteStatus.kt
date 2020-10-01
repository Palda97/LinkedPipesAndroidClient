package cz.palda97.lpclient.model.entities.execution

import androidx.room.Embedded
import androidx.room.Relation
import cz.palda97.lpclient.model.db.MarkForDeletion

data class ExecutionWithDeleteStatus (
    @Embedded
    val execution: Execution,
    @Relation(
        parentColumn = "id",
        entityColumn = "mark"
    )
    val mark: MarkForDeletion?
)
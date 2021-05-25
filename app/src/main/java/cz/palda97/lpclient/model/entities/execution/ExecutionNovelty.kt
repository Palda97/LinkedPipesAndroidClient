package cz.palda97.lpclient.model.entities.execution

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity
data class ExecutionNovelty(
    @PrimaryKey(autoGenerate = false) val id: String,
    val hasBeenShown: Boolean = false,
    val isNewlyAdded: Boolean = true
)

data class NoveltyWithExecution(
    @Embedded val novelty: ExecutionNovelty,
    @Relation(
        parentColumn = "id",
        entityColumn = "id"
    )
    val execution: Execution?
)
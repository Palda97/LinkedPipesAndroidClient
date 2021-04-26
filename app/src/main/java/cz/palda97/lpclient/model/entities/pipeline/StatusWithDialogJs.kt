package cz.palda97.lpclient.model.entities.pipeline

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Wrapper for [ConfigDownloadStatus] and [DialogJs]
 */
data class StatusWithDialogJs (
    @Embedded val status: ConfigDownloadStatus,
    @Relation(
        parentColumn = "componentId",
        entityColumn = "componentId"
    )
    val dialogJs: DialogJs?
)
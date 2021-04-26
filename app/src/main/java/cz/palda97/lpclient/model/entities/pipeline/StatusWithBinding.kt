package cz.palda97.lpclient.model.entities.pipeline

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Wrapper for [ConfigDownloadStatus] and list of [Bindings][Binding]
 */
data class StatusWithBinding (
    @Embedded val status: ConfigDownloadStatus,
    @Relation(
        parentColumn = "componentId",
        entityColumn = "templateId"
    )
    val list: List<Binding>
)
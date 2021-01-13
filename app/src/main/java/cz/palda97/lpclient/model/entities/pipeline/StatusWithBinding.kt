package cz.palda97.lpclient.model.entities.pipeline

import androidx.room.Embedded
import androidx.room.Relation

data class StatusWithBinding (
    @Embedded val status: ConfigDownloadStatus,
    @Relation(
        parentColumn = "componentId",
        entityColumn = "componentId"
    )
    val list: List<Binding>
)
package cz.palda97.lpclient.model.entities.pipeline

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Wrapper for [ConfigDownloadStatus] and list of [ConfigInputs][ConfigInput]
 */
data class StatusWithConfigInput (
    @Embedded val status: ConfigDownloadStatus,
    @Relation(
        parentColumn = "componentId",
        entityColumn = "componentId"
    )
    val list: List<ConfigInput>
)
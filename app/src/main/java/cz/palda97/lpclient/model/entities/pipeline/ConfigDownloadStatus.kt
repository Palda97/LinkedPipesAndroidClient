package cz.palda97.lpclient.model.entities.pipeline

import androidx.room.Entity
import cz.palda97.lpclient.model.repository.ComponentRepository

/**
 * Entity representing download status of either [ConfigInputs][ConfigInput],
 * [DialogJs] or [Bindings][Binding].
 */
@Entity(primaryKeys = ["componentId", "type"])
data class ConfigDownloadStatus(
    val componentId: String,
    val type: Int,
    val result: String
) {
    constructor(componentId: String, type: Int, result: ComponentRepository.StatusCode) : this(componentId, type, result.name)

    companion object {

        const val TYPE_CONFIG_INPUT = 0
        const val TYPE_DIALOG_JS = 1
        const val TYPE_BINDING = 2
    }
}
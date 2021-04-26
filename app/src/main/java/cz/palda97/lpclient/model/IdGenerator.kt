package cz.palda97.lpclient.model

import java.util.*

/**
 * Class for working with ids.
 */
object IdGenerator {

    private val timeUUID
        get() = "${System.currentTimeMillis()}-${UUID.randomUUID()}"

    /**
     * Generate id for new connection.
     */
    fun connectionId(pipelineId: String): String {
        require(pipelineId.isNotEmpty())
        return "$pipelineId/connection/$timeUUID"
    }

    /**
     * Generate id for new component.
     */
    fun componentId(pipelineId: String): String {
        require(pipelineId.isNotEmpty())
        return "$pipelineId/component/$timeUUID"
    }

    /**
     * Make an id for configuration belonging to this component.
     */
    fun configurationId(componentId: String): String {
        require(componentId.isNotEmpty())
        return "$componentId/configuration"
    }
}
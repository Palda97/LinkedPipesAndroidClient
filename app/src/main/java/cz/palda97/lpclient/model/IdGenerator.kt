package cz.palda97.lpclient.model

import java.util.*

object IdGenerator {

    private val timeUUID
        get() = "${System.currentTimeMillis()}-${UUID.randomUUID()}"

    fun connectionId(pipelineId: String): String {
        require(pipelineId.isNotEmpty())
        return "$pipelineId/connection/$timeUUID"
    }

    fun componentId(pipelineId: String): String {
        require(pipelineId.isNotEmpty())
        return "$pipelineId/component/$timeUUID"
    }

    fun configurationId(componentId: String): String {
        require(componentId.isNotEmpty())
        return "$componentId/configuration"
    }
}
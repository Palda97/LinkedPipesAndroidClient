package cz.palda97.lpclient.model.entities.execution

import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.entities.pipeline.Component
import cz.palda97.lpclient.model.travelobjects.CommonFunctions
import cz.palda97.lpclient.model.travelobjects.LdConstants

/**
 * Factory for transforming jsonLd to list of [ExecutionDetailComponent].
 */
class ExecutionDetailFactory(private val json: String, private val executionId: String, private val pipelineComponents: List<Component>) {

    /**
     * Parse jsonLd to [ExecutionDetailComponents][ExecutionDetailComponent].
     * @return list of [ExecutionDetailComponents][ExecutionDetailComponent] or null on error.
     */
    fun parse(): List<ExecutionDetailComponent>? {
        val rootArrayList = when (val res = CommonFunctions.getRootArrayList(json)) {
            is Either.Left -> return null
            is Either.Right -> res.value
        }
        if (rootArrayList.size != 1) {
            return null
        }
        val graph = CommonFunctions.prepareSemiRootElement(rootArrayList.first()) ?: return null
        val components = graph.map {
            val map = it as? Map<*, *> ?: return null
            val type = CommonFunctions.giveMeThatType(map) ?: return null
            if (type != LdConstants.TYPE_COMPONENT)
                null
            else {
                parseComponent(map) ?: return null
            }
        }
        return components.filterNotNull()
    }

    private fun parseComponent(map: Map<*, *>): ExecutionDetailComponent? {
        val id = CommonFunctions.giveMeThatId(map) ?: return null
        val status = CommonFunctions.giveMeThatString(map, LdConstants.EXECUTION_STATUS, LdConstants.ID) ?: return null
        val order = CommonFunctions.giveMeThatString(map, LdConstants.ORDER, LdConstants.VALUE)?.toIntOrNull() ?: return null
        val label = pipelineComponents.find { it.id == id }?.prefLabel ?: id
        return ExecutionDetailComponent(
            id,
            executionId,
            ExecutionStatusUtilities.fromString(status) ?: return null,
            order,
            label
        )
    }
}
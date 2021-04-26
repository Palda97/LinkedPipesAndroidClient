package cz.palda97.lpclient.viewmodel.executions

import cz.palda97.lpclient.model.entities.execution.Execution

/**
 * [Execution] prepared to be displayed in UI.
 */
data class ExecutionV(
    val id: String,
    val serverName: String,
    val pipelineName: String,
    val start: String?,
    val status: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExecutionV

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    constructor(execution: Execution) : this(
        execution.id,
        execution.serverName,
        execution.pipelineName,
        ExecutionDateParser.toViewFormat(execution.start),
        execution.status.resource
    )
}
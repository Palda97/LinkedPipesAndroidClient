package cz.palda97.lpclient.viewmodel.executiondetails

import cz.palda97.lpclient.model.entities.execution.Execution
import cz.palda97.lpclient.model.entities.execution.ExecutionDetail
import cz.palda97.lpclient.model.entities.execution.ExecutionDetailComponent
import cz.palda97.lpclient.model.repository.ExecutionDetailRepository.ExecutionDetailRepositoryStatus.Companion.toStatus
import cz.palda97.lpclient.model.repository.ExecutionDetailRepository
import cz.palda97.lpclient.viewmodel.executions.resource

data class ExecutionDetailV(
    val componentExecuted: String?,
    val componentFinished: String?,
    val componentMapped: String?,
    val componentToExecute: String?,
    val componentToMap: String?,
    val start: String?,
    val end: String?,
    val duration: String?,
    val size: String?,
    var status: Int
) {

    constructor(execution: Execution) : this(
        execution.componentExecuted?.toString(),
        execution.componentFinished?.toString(),
        execution.componentMapped?.toString(),
        execution.componentToExecute?.toString(),
        execution.componentToMap?.toString(),
        ExecutionDetailDateParser.toViewFormat(execution.start),
        ExecutionDetailDateParser.toViewFormat(execution.end),
        ExecutionDetailDateParser.duration(execution.start, execution.end),
        execution.size.sizeToString,
        execution.status.resource
    )

    companion object {
        private val Long?.sizeToString: String?
            get() = if (this == null) null else "${this / 1_000_000}"
    }
}

data class ExecutionDetailComponentV(
    val id: String,
    val status: Int,
    val label: String
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExecutionDetailComponentV

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    constructor(component: ExecutionDetailComponent) : this(
        component.id,
        component.status.resource,
        component.label
    )
}

data class ExecutionDetailViewStructure(
    val execution: ExecutionDetailV,
    val status: ExecutionDetailRepository.ExecutionDetailRepositoryStatus,
    val components: List<ExecutionDetailComponentV>
) {

    constructor(detail: ExecutionDetail) : this(
        ExecutionDetailV(detail.execution),
        detail.statusWithComponents?.status?.result.toStatus,
        detail.sortedComponents.map { ExecutionDetailComponentV(it) }
    )
}
package cz.palda97.lpclient.model.entities.execution

import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.travelobjects.CommonFunctions
import cz.palda97.lpclient.model.travelobjects.LdConstants

object ExecutionStatusUtilities {

    /**
     * @param json JsonLd with one specific execution.
     * @return [ExecutionStatus] or null if something goes wrong.
     */
    fun fromDirectRequest(json: String?): ExecutionStatus? {
        return when (val res = CommonFunctions.getRootArrayList(json)) {
            is Either.Left -> null
            is Either.Right -> {
                if (res.value.size != 1)
                    return null
                val rootArray = CommonFunctions.prepareSemiRootElement(res.value[0]) ?: return null
                if (rootArray.size < 1)
                    return null
                val rootMap = rootArray[0] as? Map<*, *> ?: return null
                fromMap(rootMap)
            }
        }
    }

    private fun fromMap(map: Map<*, *>): ExecutionStatus? {
        val statusString =
            CommonFunctions.giveMeThatString(map, LdConstants.EXECUTION_STATUS, LdConstants.ID)
                ?: return null
        return fromString(statusString)
    }

    private const val TAG = "ExecutionStatusUtilities"
    private val l = Injector.generateLogFunction(TAG)

    /**
     * Converts executions status id to [ExecutionStatus].
     * @return [ExecutionStatus] or null when id is not matched.
     */
    fun fromString(string: String?): ExecutionStatus? = when (string) {
        LdConstants.EXECUTION_STATUS_FINISHED -> ExecutionStatus.FINISHED
        LdConstants.EXECUTION_STATUS_FAILED -> ExecutionStatus.FAILED
        LdConstants.EXECUTION_STATUS_RUNNING -> ExecutionStatus.RUNNING
        LdConstants.EXECUTION_STATUS_CANCELLED -> ExecutionStatus.CANCELLED
        LdConstants.EXECUTION_STATUS_DANGLING -> ExecutionStatus.DANGLING
        LdConstants.EXECUTION_STATUS_CANCELLING -> ExecutionStatus.CANCELLING
        LdConstants.EXECUTION_STATUS_QUEUED -> ExecutionStatus.QUEUED
        LdConstants.EXECUTION_STATUS_MAPPED -> ExecutionStatus.MAPPED
        else -> {
            l("fromString is returning null because of: $string")
            null
        }
    }
}
package cz.palda97.lpclient.model.entities.execution

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import cz.palda97.lpclient.model.DateParser
import cz.palda97.lpclient.model.travelobjects.LdConstants
import java.util.*

/**
 * Entity representing pipeline's execution.
 */
@Entity
data class Execution(
    @PrimaryKey(autoGenerate = false) val id: String,
    val componentExecuted: Int?,
    val componentFinished: Int?,
    val componentMapped: Int?,
    val componentToExecute: Int?,
    val componentToMap: Int?,
    val end: Date?,
    val size: Long?,
    val start: Date?,
    var status: ExecutionStatus,
    val serverId: Long
) {
    @Ignore
    var serverName: String = ""

    var pipelineId: String = ""

    var pipelineName: String = ""

    /**
     * @see idNumberFun
     */
    val idNumber: String
        get() = idNumberFun(id)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Execution

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    companion object {

        /**
         * @param fullId Id of the execution.
         * @return The part of id behind the last slash.
         */
        fun idNumberFun(fullId: String) = fullId.split("/").last()
    }
}

enum class ExecutionStatus {
    FINISHED, FAILED, RUNNING, CANCELLED, DANGLING, CANCELLING, QUEUED, MAPPED;

    companion object {

        val ExecutionStatus?.isDone: Boolean
            get() = when (this) {
                null -> false
                QUEUED -> false
                RUNNING -> false
                else -> true
            }
    }
}

/**
 * Class used for parsing [execution overview][ExecutionOverview].
 */
data class PipelineProgress(
    val total: Int,
    val current: Int,
    val total_map: Int,
    val current_mapped: Int,
    val current_executed: Int
)

/**
 * Class used while parsing [Execution] from execution overview.
 */
data class ExecutionOverview(
    /*@SerializedName("pipeline")
    val pipelineId: Map<String, String>,*/
    @SerializedName("execution")
    val executionId: Map<String, String>,
    val executionStarted: String,
    val executionFinished: String,
    val status: Map<String, String>,
    val pipelineProgress: PipelineProgress,
    val directorySize: Long
) {

    /**
     * Create [Execution] from this instance.
     */
    fun execution(serverId: Long, pipelineName: String, pipelineId: String): Execution? {
        return Execution(
            executionId[LdConstants.ID] ?: return null,
            pipelineProgress.current_executed,
            pipelineProgress.current,
            pipelineProgress.current_mapped,
            pipelineProgress.total,
            pipelineProgress.total_map,
            DateParser.toDateWithoutTimezone(executionFinished),
            directorySize,
            DateParser.toDateWithoutTimezone(executionStarted),
            ExecutionStatusUtilities.fromString(status[LdConstants.ID]) ?: return null,
            serverId
        ).apply {
            this.pipelineName = pipelineName
            this.pipelineId = pipelineId
        }
    }
}

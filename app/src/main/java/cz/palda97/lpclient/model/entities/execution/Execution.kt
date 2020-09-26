package cz.palda97.lpclient.model.entities.execution

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.*

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
    val status: ExecutionStatus,
    val serverId: Long
) {
    @Ignore
    var serverName: String = ""

    var pipelineId: String = ""

    var pipelineName: String = ""

    var deleted: Boolean = false

    val idNumber: String
        get() = id.split("/").last()

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


}

enum class ExecutionStatus {
    FINISHED, FAILED, RUNNING, CANCELLED, DANGLING
}
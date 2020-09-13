package cz.palda97.lpclient.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Execution(
    @PrimaryKey(autoGenerate = false) val id: String,
    val componentExecuted: Int,
    val componentFinished: Int,
    val componentMapped: Int,
    val componentToExecute: Int,
    val componentToMap: Int,
    val end: Date,
    val size: Long,
    val start: Date,
    val pipelineId: String,
    val status: ExecutionStatus,
    val serverId: Long
) {
    @Ignore
    var serverName: String = ""

    var pipelineName: String = ""

    var deleted: Boolean = false

    val idNumber: String
        get() = id.split("/").last()
}

enum class ExecutionStatus {
    FINISHED, FAILED, RUNNING
}
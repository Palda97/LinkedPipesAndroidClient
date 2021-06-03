package cz.palda97.lpclient.model.entities.execution

import androidx.room.*
import com.google.gson.annotations.SerializedName
import cz.palda97.lpclient.model.DateParser
import cz.palda97.lpclient.model.travelobjects.LdConstants

@Entity(primaryKeys = ["id", "executionId"])
data class ExecutionDetailComponent(
    val id: String,
    val executionId: String,
    val status: ExecutionStatus,
    val order: Int,
    val label: String
)

@Entity
data class ExecutionDetailStatus(
    @PrimaryKey(autoGenerate = false) val executionId: String,
    val result: String
)

data class ExecutionDetailStatusWithComponents(
    @Embedded val status: ExecutionDetailStatus,
    @Relation(
        parentColumn = "executionId",
        entityColumn = "executionId"
    )
    val components: List<ExecutionDetailComponent>
)

data class ExecutionDetail(
    @Embedded val execution: Execution,
    @Relation(
        entity = ExecutionDetailStatus::class,
        parentColumn = "id",
        entityColumn = "executionId"
    )
    val statusWithComponents: ExecutionDetailStatusWithComponents?
) {
    @Ignore
    val sortedComponents = statusWithComponents?.components?.sortedBy { it.order } ?: emptyList()
}

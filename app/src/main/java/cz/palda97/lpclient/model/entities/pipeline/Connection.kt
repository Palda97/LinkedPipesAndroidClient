package cz.palda97.lpclient.model.entities.pipeline

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Connection(
    val sourceBinding: String,
    val sourceComponentId: String,
    val targetBinding: String,
    val targetComponentId: String,
    val vertexIds: List<String>,
    @PrimaryKey(autoGenerate = false) val id: String
)
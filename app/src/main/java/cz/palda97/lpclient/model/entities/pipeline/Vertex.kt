package cz.palda97.lpclient.model.entities.pipeline

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing connection's vertex.
 */
@Entity
data class Vertex(
    val order: Int,
    var x: Int, var y: Int,
    @PrimaryKey(autoGenerate = false) val id: String
)
package cz.palda97.lpclient.model.entities.pipelineview

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
data class PipelineView(
    var prefLabel: String,
    @PrimaryKey(autoGenerate = false) val id: String,
    val serverId: Long
) {
    @Ignore
    var serverName: String = ""

    val idNumber: String
        get() = id.split("/").last()

    var version: Int? = null
}
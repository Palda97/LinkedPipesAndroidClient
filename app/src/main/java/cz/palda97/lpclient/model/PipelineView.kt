package cz.palda97.lpclient.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
data class PipelineView(
    val prefLabel: String,
    @PrimaryKey(autoGenerate = false) val id: String,
    val serverId: Long
) {
    @Ignore
    var serverName: String = ""

    var deleted: Boolean = false
}
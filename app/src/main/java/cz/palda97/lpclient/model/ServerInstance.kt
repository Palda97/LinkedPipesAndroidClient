package cz.palda97.lpclient.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ServerInstance(
    val name: String = "",
    @PrimaryKey(autoGenerate = false) val url: String = "",
    val active: Boolean = true,
    val description: String = ""
)
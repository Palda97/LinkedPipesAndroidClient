package cz.palda97.lpclient.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class ServerInstance(
    val name: String = "",
    @PrimaryKey(autoGenerate = false) val url: String = "",
    val active: Boolean = true,
    val description: String = ""
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ServerInstance

        if (url != other.url) return false

        return true
    }

    override fun hashCode(): Int {
        return url.hashCode()
    }

    constructor (serverInstance: ServerInstance) : this(
        serverInstance.name,
        serverInstance.url,
        serverInstance.active,
        serverInstance.description
    )
}
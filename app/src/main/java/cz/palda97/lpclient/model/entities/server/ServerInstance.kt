package cz.palda97.lpclient.model.entities.server

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ServerInstance(
    val name: String = "",
    val url: String = "",
    val active: Boolean = true,
    val description: String = ""
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ServerInstance

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "ServerInstance(name='$name', url='$url', active=$active, description='$description', id=$id)"
    }

}
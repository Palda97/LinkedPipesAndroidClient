package cz.palda97.lpclient.model.entities.server

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ServerInstance(
    val name: String = "",
    val url: String = "",
    val active: Boolean = true,
    val description: String = "",
    val auth: Boolean = false
) {
    var frontend: Int? = null
    val frontendUrl: String
        get() {
            frontend?.let {
                return "$url:$it"
            }
            return url
        }

    var username: String = ""
    var password: String = ""
    val credentials: Pair<String, String>?
        get() = if (auth) username to password else null

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
}
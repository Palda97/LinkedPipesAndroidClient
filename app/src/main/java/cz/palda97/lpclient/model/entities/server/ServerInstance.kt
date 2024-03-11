package cz.palda97.lpclient.model.entities.server

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * Entity representing server instance.
 * @property frontendUrl Url containing the [frontend] port.
 */
@Entity
data class ServerInstance @Ignore constructor(
    var name: String = "",
    var url: String = "",
    var active: Boolean = true,
    var description: String = "",
    var auth: Boolean = false,
    var changedSince: Long? = null
) {
    constructor() : this(
        name = "",
        url = "",
        active = true,
        description = "",
        auth = false,
        changedSince = null,
    )
    var frontend: Int? = null
    val frontendUrl: String
        get() {
            val urlNoSlash = url.removeSuffix("/")
            frontend?.let {
                return "$urlNoSlash:$it"
            }
            return urlNoSlash
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

    companion object {

        /**
         * Check if there is a web protocol present in the string.
         * @receiver The source string.
         * @return The source string optionally with https protocol prefixed.
         */
        val String.urlWithFixedProtocol: String
            get() =
                if (contains("://"))
                    this
                else
                    "https://$this"
    }
}
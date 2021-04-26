package cz.palda97.lpclient.model.entities.pipeline

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing pipeline tag.
 */
@Entity
data class Tag(
    @PrimaryKey(autoGenerate = false) val value: String
) {
    override fun toString(): String = value
}
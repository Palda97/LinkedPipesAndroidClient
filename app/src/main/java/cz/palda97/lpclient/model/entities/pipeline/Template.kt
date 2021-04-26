package cz.palda97.lpclient.model.entities.pipeline

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing template.
 */
@Entity
data class Template(
    val configurationId: String?,
    val templateId: String,
    val prefLabel: String,
    val description: String?,
    @PrimaryKey(autoGenerate = false) val id: String
)
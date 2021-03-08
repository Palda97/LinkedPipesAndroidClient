package cz.palda97.lpclient.model.entities.pipeline

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SameAs(
    @PrimaryKey(autoGenerate = false) val id: String,
    val sameAs: String
)
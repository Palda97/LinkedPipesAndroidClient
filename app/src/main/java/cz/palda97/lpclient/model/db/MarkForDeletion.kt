package cz.palda97.lpclient.model.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MarkForDeletion(
    @PrimaryKey(autoGenerate = false) val mark: String
)
package cz.palda97.lpclient.model.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity used to store keys of items determined for deletion.
 * Marks are intended to be stored in database while the UNDO button
 * is visible. If the application is killed before the delete request
 * is sent, the mark alongside with the item for deletion stays in
 * database and the delete request is sent on the next app start.
 */
@Entity
data class MarkForDeletion(
    @PrimaryKey(autoGenerate = false) val mark: String
)
package cz.palda97.lpclient.model.entities.pipeline

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Profile(
    val repoPolicyId: String?,
    val repoTypeId: String?,
    @PrimaryKey(autoGenerate = false) val id: String
)
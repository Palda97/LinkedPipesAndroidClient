package cz.palda97.lpclient.model.entities.possiblecomponent

import androidx.room.Entity
import androidx.room.PrimaryKey
import cz.palda97.lpclient.model.repository.PossibleComponentRepository

/**
 * Entity representing download status of [PossibleComponent]s.
 */
@Entity
data class PossibleStatus(
    @PrimaryKey(autoGenerate = false) val serverId: Long,
    val result: String
) {
    constructor(serverId: Long, result: PossibleComponentRepository.StatusCode): this(serverId, result.name)
}
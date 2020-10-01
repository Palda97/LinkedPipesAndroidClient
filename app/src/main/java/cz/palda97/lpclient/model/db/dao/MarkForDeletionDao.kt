package cz.palda97.lpclient.model.db.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
abstract class MarkForDeletionDao {

    @Query("insert into markfordeletion values (:id)")
    abstract suspend fun markForDeletion(id: String)

    @Query("delete from markfordeletion where mark = :id")
    abstract suspend fun unMarkForDeletion(id: String)

    suspend fun delete(id: String) = unMarkForDeletion(id)
}
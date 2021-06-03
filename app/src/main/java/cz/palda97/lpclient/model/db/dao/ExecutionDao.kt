package cz.palda97.lpclient.model.db.dao

import androidx.room.*
import cz.palda97.lpclient.model.entities.execution.Execution

@Dao
abstract class ExecutionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(list: List<Execution>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(list: Execution)

    @Delete
    abstract suspend fun delete(execution: Execution)

    @Query("delete from execution where id in (:list)")
    abstract suspend fun delete(list: List<String>)

    @Query("delete from execution")
    abstract suspend fun deleteAll()

    /**
     * Deletes all executions and inserts new ones.
     * @param list Executions to be inserted to database.
     */
    @Transaction
    open suspend fun renewal(list: List<Execution>) {
        deleteAll()
        insert(list)
    }

    /**
     * Select all executions with that have matching deletion mark stored in database.
     */
    @Query("select * from execution join markfordeletion on Execution.id = MarkForDeletion.mark")
    abstract suspend fun selectDeleted(): List<Execution>

    @Query("select * from execution where id = :id")
    abstract suspend fun findById(id: String): Execution?

    @Query("delete from execution where serverId = :serverId")
    abstract suspend fun deleteByServer(serverId: Long)

    /**
     * Insert executions that are not in database and ignore those which are already stored
     * (don't rewrite executions).
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun silentInsert(list: List<Execution>)
}
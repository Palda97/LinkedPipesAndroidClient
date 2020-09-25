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

    @Query("delete from execution")
    abstract suspend fun deleteAll()

    @Transaction
    open suspend fun renewal(list: List<Execution>) {
        deleteAll()
        insert(list)
    }

    @Query("select * from execution where deleted = 1")
    abstract suspend fun selectDeleted(): List<Execution>
}
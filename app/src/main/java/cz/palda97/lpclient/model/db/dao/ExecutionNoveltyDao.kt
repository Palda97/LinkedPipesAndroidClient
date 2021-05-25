package cz.palda97.lpclient.model.db.dao

import androidx.room.*
import cz.palda97.lpclient.model.entities.execution.Execution
import cz.palda97.lpclient.model.entities.execution.ExecutionNovelty
import cz.palda97.lpclient.model.entities.execution.NoveltyWithExecution

@Dao
abstract class ExecutionNoveltyDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertNoveltySoft(list: List<ExecutionNovelty>)

    @Transaction
    @Query("select * from executionnovelty where isNewlyAdded = 1")
    abstract suspend fun selectNewlyAddedWithExecution(): List<NoveltyWithExecution>

    @Query("update executionnovelty set isNewlyAdded = 0")
    abstract suspend fun age()

    @Transaction
    open suspend fun filterReallyNew(list: List<ExecutionNovelty>): List<NoveltyWithExecution> {
        insertNoveltySoft(list)
        val reallyNew = selectNewlyAddedWithExecution()
        age()
        return reallyNew
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertExecutions(executions: List<Execution>)
}
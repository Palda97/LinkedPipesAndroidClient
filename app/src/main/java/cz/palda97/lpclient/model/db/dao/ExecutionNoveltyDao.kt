package cz.palda97.lpclient.model.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import cz.palda97.lpclient.model.entities.execution.ExecutionNovelty
import cz.palda97.lpclient.model.entities.execution.NoveltyWithExecution

@Dao
abstract class ExecutionNoveltyDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(list: List<ExecutionNovelty>)

    @Transaction
    @Query("select * from executionnovelty where isNewlyAdded = 1")
    abstract suspend fun selectNewlyAddedWithExecution(): List<NoveltyWithExecution>

    @Query("update executionnovelty set isNewlyAdded = 0")
    abstract suspend fun age()

    @Transaction
    open suspend fun filterReallyNew(list: List<ExecutionNovelty>): List<NoveltyWithExecution> {
        insert(list)
        val reallyNew = selectNewlyAddedWithExecution()
        age()
        return reallyNew
    }

    @Transaction
    @Query("select * from executionnovelty")
    abstract suspend fun selectNoveltyWithExecutionList(): List<NoveltyWithExecution>

    @Delete
    abstract suspend fun delete(list: List<ExecutionNovelty>)

    @Query("select * from executionnovelty where hasBeenShown = 0")
    abstract fun liveRecent(): LiveData<List<ExecutionNovelty>>

    @Query("update executionnovelty set hasBeenShown = 1 where id in (:ids)")
    abstract suspend fun resetRecentLimited(ids: List<String>)

    @Transaction
    open suspend fun resetRecent(ids: List<String>) {
        val parts = ids.chunked(500)
        parts.forEach {
            resetRecentLimited(it)
        }
    }

    @Query("update executionnovelty set hasBeenShown = 1")
    abstract suspend fun resetAllRecent()
}
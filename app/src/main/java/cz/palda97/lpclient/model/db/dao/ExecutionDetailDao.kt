package cz.palda97.lpclient.model.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import cz.palda97.lpclient.model.entities.execution.ExecutionDetail
import cz.palda97.lpclient.model.entities.execution.ExecutionDetailComponent
import cz.palda97.lpclient.model.entities.execution.ExecutionDetailStatus

@Dao
abstract class ExecutionDetailDao {

    /*@Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertComponent(component: ExecutionDetailComponent)*/

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertComponent(component: List<ExecutionDetailComponent>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertStatus(status: ExecutionDetailStatus)

    /*@Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertStatus(statuses: List<ExecutionDetailStatus>)*/

    @Transaction
    @Query("select * from execution where id = :executionId")
    abstract fun liveExecutionDetail(executionId: String): LiveData<ExecutionDetail>
}
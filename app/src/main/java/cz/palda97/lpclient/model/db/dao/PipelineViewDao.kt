package cz.palda97.lpclient.model.db.dao

import androidx.room.*
import cz.palda97.lpclient.model.PipelineView

@Dao
abstract class PipelineViewDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertList(list: List<PipelineView>)

    @Delete
    abstract suspend fun deletePipelineView(pipelineView: PipelineView)

    @Query("delete from pipelineview")
    abstract suspend fun deleteAll()

    @Transaction
    open suspend fun deleteAndInsertPipelineViews(list: List<PipelineView>) {
        deleteAll()
        insertList(list)
    }
}
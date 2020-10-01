package cz.palda97.lpclient.model.db.dao

import androidx.room.*
import cz.palda97.lpclient.model.entities.pipeline.PipelineView

@Dao
abstract class PipelineViewDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertList(list: List<PipelineView>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(pipelineView: PipelineView)

    @Delete
    abstract suspend fun deletePipelineView(pipelineView: PipelineView)

    @Query("delete from pipelineview")
    abstract suspend fun deleteAll()

    @Transaction
    open suspend fun deleteAndInsertPipelineViews(list: List<PipelineView>) {
        deleteAll()
        insertList(list)
    }

    @Query("select * from pipelineview where id = :id")
    abstract suspend fun findPipelineViewById(id: String): PipelineView?

    @Query("select * from pipelineview join markfordeletion on PipelineView.id = MarkForDeletion.mark")
    abstract suspend fun selectDeleted(): List<PipelineView>
}
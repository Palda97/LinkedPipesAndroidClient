package cz.palda97.lpclient.model.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.model.entities.execution.ServerWithExecutions
import cz.palda97.lpclient.model.entities.pipelineview.ServerWithPipelineViews

@Dao
abstract class ServerInstanceDao {

    @Query("select * from serverinstance order by id asc")
    abstract fun serverList(): LiveData<List<ServerInstance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertServer(server: ServerInstance): Long

    @Delete
    abstract suspend fun deleteServer(server: ServerInstance)

    @Query("delete from serverinstance")
    abstract suspend fun deleteAll()

    @Query("select * from serverinstance where active = 1 order by id asc")
    abstract fun activeServersOnly(): LiveData<List<ServerInstance>>

    /**
     * Returns server instances that match either name or url and doesn't match
     * unwanted name and url at the same time.
     */
    @Query("select * from serverinstance where (url = :matchUrl and url != :exceptUrl) or (name = :matchName and name != :exceptName)")
    abstract suspend fun matchExcept(
        matchUrl: String,
        matchName: String,
        exceptUrl: String = "",
        exceptName: String = ""
    ): List<ServerInstance>

    @Transaction
    @Query("select * from serverinstance order by id asc")
    abstract fun serverListWithPipelineViews(): LiveData<List<ServerWithPipelineViews>>

    @Transaction
    @Query("select * from serverinstance where active = 1 order by id asc")
    abstract fun activeServerListWithPipelineViews(): LiveData<List<ServerWithPipelineViews>>

    @Query("select * from serverinstance where id = :id")
    abstract fun findById(id: Long): ServerInstance?

    @Transaction
    @Query("select * from serverinstance where active = 1 order by id asc")
    abstract fun activeServerListWithExecutions(): LiveData<List<ServerWithExecutions>>

    @Query("select * from serverinstance where active = 1 order by id asc")
    abstract suspend fun activeServers(): List<ServerInstance>
}
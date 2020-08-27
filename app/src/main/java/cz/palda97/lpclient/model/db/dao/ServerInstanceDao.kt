package cz.palda97.lpclient.model.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import cz.palda97.lpclient.model.ServerInstance

@Dao
abstract class ServerInstanceDao {
    @Query("select * from serverinstance order by name asc")
    abstract fun serverList(): LiveData<List<ServerInstance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertServer(server: ServerInstance)

    @Delete
    abstract suspend fun deleteServer(server: ServerInstance)

    @Query("delete from serverinstance")
    abstract suspend fun deleteAll()

    @Query("select * from serverinstance where active = 1 order by name asc")
    abstract fun activeServersOnly(): LiveData<List<ServerInstance>>

    @Query("select * from serverinstance where (url = :matchUrl and url != :exceptUrl) or (name = :matchName and name != :exceptName)")
    abstract suspend fun matchExcept(matchUrl: String, matchName: String, exceptUrl: String = "", exceptName: String = ""): List<ServerInstance>
}
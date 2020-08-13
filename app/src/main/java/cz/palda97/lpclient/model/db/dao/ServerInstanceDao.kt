package cz.palda97.lpclient.model.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import cz.palda97.lpclient.model.ServerInstance

@Dao
abstract class ServerInstanceDao {
    @Query("select * from serverinstance order by name asc")
    abstract fun serverList(): LiveData<List<ServerInstance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertServer(server: ServerInstance)

    @Delete
    abstract fun deleteFeed(server: ServerInstance)

    @Query("delete from serverinstance")
    abstract fun deleteAll()

    @Query("select * from serverinstance where active = 1 order by name asc")
    abstract fun activeServersOnly(): LiveData<List<ServerInstance>>
}
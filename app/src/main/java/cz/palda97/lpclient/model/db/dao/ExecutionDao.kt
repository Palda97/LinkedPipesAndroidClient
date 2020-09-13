package cz.palda97.lpclient.model.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import cz.palda97.lpclient.model.Execution

@Dao
abstract class ExecutionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertList(list: List<Execution>)
}
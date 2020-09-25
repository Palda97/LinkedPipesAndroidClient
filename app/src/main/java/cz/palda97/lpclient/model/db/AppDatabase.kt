package cz.palda97.lpclient.model.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cz.palda97.lpclient.model.entities.execution.Execution
import cz.palda97.lpclient.model.entities.pipeline.PipelineView
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.model.db.dao.ExecutionDao
import cz.palda97.lpclient.model.db.dao.PipelineViewDao
import cz.palda97.lpclient.model.db.dao.ServerInstanceDao

/**
 * Application database implemented with Room
 */
@Database(entities = [ServerInstance::class, PipelineView::class, Execution::class], version = 5, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun serverDao(): ServerInstanceDao
    abstract fun pipelineViewDao(): PipelineViewDao
    abstract fun executionDao(): ExecutionDao

    companion object {

        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            if (instance == null) {
                synchronized(AppDatabase::class.java) {
                    if (instance == null) {
                        instance =
                            Room.databaseBuilder(context, AppDatabase::class.java, "database")
                                .fallbackToDestructiveMigration()
                                .build()
                    }
                }
            }
            return instance!!
        }
    }
}
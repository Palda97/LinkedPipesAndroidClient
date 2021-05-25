package cz.palda97.lpclient.model.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cz.palda97.lpclient.model.db.dao.*
import cz.palda97.lpclient.model.entities.execution.Execution
import cz.palda97.lpclient.model.entities.execution.ExecutionDetailComponent
import cz.palda97.lpclient.model.entities.execution.ExecutionDetailStatus
import cz.palda97.lpclient.model.entities.execution.ExecutionNovelty
import cz.palda97.lpclient.model.entities.pipeline.*
import cz.palda97.lpclient.model.entities.pipelineview.PipelineView
import cz.palda97.lpclient.model.entities.possiblecomponent.PossibleComponent
import cz.palda97.lpclient.model.entities.possiblecomponent.PossibleStatus
import cz.palda97.lpclient.model.entities.server.ServerInstance

/**
 * Application database implemented with Room
 */
@Database(
    entities = [ServerInstance::class, PipelineView::class, Execution::class, MarkForDeletion::class,
        ExecutionDetailStatus::class, ExecutionDetailComponent::class,
        ExecutionNovelty::class,
        Binding::class, Component::class, ConfigInput::class, Configuration::class, Connection::class, DialogJs::class, Profile::class, Template::class, Vertex::class, ConfigDownloadStatus::class,
        PossibleComponent::class, PossibleStatus::class,
        SameAs::class, Tag::class],
    version = 18,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun serverDao(): ServerInstanceDao
    abstract fun pipelineViewDao(): PipelineViewDao
    abstract fun executionDao(): ExecutionDao
    abstract fun markForDeletionDao(): MarkForDeletionDao
    abstract fun pipelineDao(): PipelineDao
    abstract fun executionDetailDao(): ExecutionDetailDao
    abstract fun executionNoveltyDao(): ExecutionNoveltyDao

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

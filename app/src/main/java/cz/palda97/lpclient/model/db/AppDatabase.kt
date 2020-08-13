package cz.palda97.lpclient.model.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import cz.palda97.lpclient.model.ServerInstance
import cz.palda97.lpclient.model.db.dao.ServerInstanceDao

/**
 * Application database implemented with Room
 */
@Database(entities = [ServerInstance::class], version = 0, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    abstract fun serverDao(): ServerInstanceDao

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
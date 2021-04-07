package cz.palda97.lpclient

//import androidx.test.platform.app.InstrumentationRegistry
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cz.palda97.lpclient.model.db.AppDatabase
import org.junit.runner.RunWith
import java.util.concurrent.Executors

@RunWith(AndroidJUnit4::class)
abstract class AndroidTest {

    protected val newDb
        get() = Room
            .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), AppDatabase::class.java)
            .setTransactionExecutor(Executors.newSingleThreadExecutor())
            .build()
}
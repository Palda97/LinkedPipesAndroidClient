package cz.palda97.lpclient

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import cz.palda97.lpclient.model.db.AppDatabase
import org.junit.After
import org.junit.Before
import org.junit.Rule
import java.io.IOException

abstract class TestWithDb
    : AndroidTest() {

    @Rule @JvmField val rule = InstantTaskExecutorRule()

    protected lateinit var db: AppDatabase

    @Before
    fun createDb() {
        db = newDb
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }
}
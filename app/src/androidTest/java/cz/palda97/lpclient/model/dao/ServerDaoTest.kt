package cz.palda97.lpclient.model.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import cz.palda97.lpclient.AndroidTest
import cz.palda97.lpclient.*
import cz.palda97.lpclient.model.db.AppDatabase
import cz.palda97.lpclient.model.db.dao.ServerInstanceDao
import cz.palda97.lpclient.model.entities.server.ServerInstance
import kotlinx.coroutines.runBlocking
import org.junit.*
import java.io.IOException
import org.junit.Assert.*

class ServerDaoTest
    : AndroidTest() {

    @Rule @JvmField val rule = InstantTaskExecutorRule()

    private lateinit var db: AppDatabase
    private lateinit var serverDao: ServerInstanceDao

    private val serverFullList = listOf(
        ServerInstance("1", "url1", true, "", false).apply { id = 1 },
        ServerInstance("2", "url2", true, "", false).apply { id = 2 },
        ServerInstance("3", "url3", true, "", false).apply { id = 3 },
        ServerInstance("4", "url4", false, "", false).apply { id = 4 },
        ServerInstance("5", "url5", false, "", false).apply { id = 5 }
    )

    @Before
    fun createDbAddServers() {
        db = newDb
        serverDao = db.serverDao()

        runBlocking {
            serverFullList.forEach {
                serverDao.insertServer(it)
            }
        }
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertionAndSelectAll() {
        val list = serverDao.serverList().await()
        assertNotNull(list)
        list!!
        assertEquals(serverFullList.size, list.size)
    }

    @Test
    fun deleteAndFindById() {
        runBlocking { serverDao.deleteServer(serverFullList[2]) }
        assertNotNull(runBlocking { serverDao.findById(1) })
        assertNotNull(runBlocking { serverDao.findById(2) })
        assertNull(runBlocking { serverDao.findById(3) })
        assertNotNull(runBlocking { serverDao.findById(4) })
        assertNotNull(runBlocking { serverDao.findById(5) })
        runBlocking { serverDao.deleteAll() }
        val list = serverDao.serverList().await()
        assertEquals(0, list?.size)
    }

    @Test
    fun activeServers() {
        val listDirect = runBlocking { serverDao.activeServers() }
        val listLive = serverDao.activeServersOnly().await()
        assertEquals(3, listDirect.size)
        assertEquals(3, listLive?.size)
    }

    @Test
    fun matchExcept() {
        val a = runBlocking { serverDao.matchExcept("new url", "1", "url1", "1") }
        assertTrue(a.isEmpty())
        val b = runBlocking { serverDao.matchExcept("url2", "1", "url1", "1") }
        assertEquals(1, b.size)
    }
}
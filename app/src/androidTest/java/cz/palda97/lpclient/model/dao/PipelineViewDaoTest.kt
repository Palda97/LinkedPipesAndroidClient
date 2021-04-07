package cz.palda97.lpclient.model.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import cz.palda97.lpclient.AndroidTest
import cz.palda97.lpclient.*
import cz.palda97.lpclient.model.db.AppDatabase
import cz.palda97.lpclient.model.db.dao.MarkForDeletionDao
import cz.palda97.lpclient.model.db.dao.PipelineViewDao
import cz.palda97.lpclient.model.db.dao.ServerInstanceDao
import cz.palda97.lpclient.model.entities.pipelineview.PipelineView
import cz.palda97.lpclient.model.entities.pipelineview.ServerWithPipelineViews
import cz.palda97.lpclient.model.entities.server.ServerInstance
import kotlinx.coroutines.runBlocking
import org.junit.*
import java.io.IOException
import org.junit.Assert.*

class PipelineViewDaoTest
    : AndroidTest() {

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private lateinit var db: AppDatabase
    private lateinit var serverDao: ServerInstanceDao
    private lateinit var pipelineDao: PipelineViewDao
    private lateinit var markDao: MarkForDeletionDao

    private val server666 = ServerInstance("server666", active = false).apply { id = 666L }
    private val server777 = ServerInstance("server777").apply { id = 777L }

    private val pipelineViewFullList = listOf(
        PipelineView("0", "0", server666.id),
        PipelineView("1", "1", server666.id),
        PipelineView("2", "2", server777.id),
        PipelineView("3", "3", server777.id),
        PipelineView("4", "4", server777.id)
    )

    @Before
    fun createDbAddServers() {
        db = newDb
        serverDao = db.serverDao()
        pipelineDao = db.pipelineViewDao()
        markDao = db.markForDeletionDao()

        runBlocking {
            serverDao.insertServer(server666)
            serverDao.insertServer(server777)
        }
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    private fun List<ServerWithPipelineViews>.extractPipelines(server: ServerInstance) =
        find { it.server.id == server.id }?.pipelineViewList?.map { it.pipelineView } ?: emptyList()

    @Test
    fun insertAndActive() {
        runBlocking { pipelineDao.insertList(pipelineViewFullList) }
        val all = serverDao.serverListWithPipelineViews().await()
        val active = serverDao.activeServerListWithPipelineViews().await()
        assertNotNull(all)
        assertNotNull(active)
        all!!
        active!!
        val all666 = all.extractPipelines(server666)
        val all777 = all.extractPipelines(server777)
        val active666 = active.extractPipelines(server666)
        val active777 = active.extractPipelines(server777)
        assertEquals(2, all666.size)
        assertEquals(3, all777.size)
        assertEquals(0, active666.size)
        assertEquals(3, active777.size)
    }

    private val allPipelineViews
        get() = serverDao.serverListWithPipelineViews().await()!!.flatMap { it.pipelineViewList }.map { it.pipelineView }

    @Test
    fun delete() {
        runBlocking {
            pipelineDao.insertList(pipelineViewFullList)
            pipelineDao.deletePipelineView(pipelineViewFullList[4])
        }
        val a = allPipelineViews
        assertListContentMatch(pipelineViewFullList - pipelineViewFullList[4], a)
        runBlocking { pipelineDao.deleteByServer(server777.id) }
        val b = allPipelineViews
        assertListContentMatch(pipelineViewFullList.dropLast(3), b)
        val smallList = listOf(
            PipelineView("small0", "small0", server666.id),
            PipelineView("small1", "small1", server777.id)
        )
        runBlocking { pipelineDao.deleteAndInsertPipelineViews(smallList) }
        val c = allPipelineViews
        assertListContentMatch(smallList, c)
        runBlocking { pipelineDao.deleteAll() }
        val d = allPipelineViews
        assertTrue(d.isEmpty())
    }

    @Test
    fun findById() {
        val a = runBlocking {
            pipelineDao.insert(pipelineViewFullList.first())
            pipelineDao.findPipelineViewById("0")
        }
        val b = runBlocking { pipelineDao.findPipelineViewById("1") }
        assertNotNull(a)
        assertNull(b)
        assertEquals(pipelineViewFullList.first(), a)
    }

    @Test
    fun markTest() {
        val list = runBlocking {
            pipelineDao.insertList(pipelineViewFullList)
            markDao.markForDeletion("2")
            pipelineDao.selectDeleted()
        }
        assertEquals(1, list.size)
        assertEquals("2", list.first().id)
    }
}
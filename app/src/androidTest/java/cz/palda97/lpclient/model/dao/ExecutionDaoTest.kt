package cz.palda97.lpclient.model.dao

import cz.palda97.lpclient.*
import cz.palda97.lpclient.model.db.dao.ExecutionDao
import cz.palda97.lpclient.model.db.dao.MarkForDeletionDao
import cz.palda97.lpclient.model.db.dao.ServerInstanceDao
import cz.palda97.lpclient.model.entities.execution.Execution
import cz.palda97.lpclient.model.entities.execution.ExecutionStatus
import cz.palda97.lpclient.model.entities.server.ServerInstance
import kotlinx.coroutines.*
import org.junit.*
import org.junit.Assert.*

class ExecutionDaoTest
    : TestWithDb() {

    private lateinit var executionDao: ExecutionDao
    private lateinit var serverDao: ServerInstanceDao
    private lateinit var markDao: MarkForDeletionDao

    private val server666 = ServerInstance("server666").apply { id = 666L }
    private val server777 = ServerInstance("server777").apply { id = 777L }

    @Before
    fun addServers() {
        executionDao = db.executionDao()
        serverDao = db.serverDao()
        markDao = db.markForDeletionDao()

        runBlocking {
            serverDao.insertServer(server666)
            serverDao.insertServer(server777)
        }
    }

    private val executionFullList = listOf(
        Execution(
            "0",
            null, null, null, null, null, null, null, null,
            ExecutionStatus.FINISHED,
            server666.id
        ),
        Execution(
            "1",
            null, null, null, null, null, null, null, null,
            ExecutionStatus.FINISHED,
            server777.id
        ),
        Execution(
            "2",
            null, null, null, null, null, null, null, null,
            ExecutionStatus.FAILED,
            server777.id
        ),
        Execution(
            "3",
            null, null, null, null, null, null, null, null,
            ExecutionStatus.FAILED,
            server777.id
        )
    )

    private fun assertDbExecutions(size666: Int, size777: Int) {
        val serversWithExecutions = serverDao.activeServerListWithExecutions().await()
        serversWithExecutions!!
        val s666 = serversWithExecutions.find { it.server.id == server666.id }!!.executionList
        val s777 = serversWithExecutions.find { it.server.id == server777.id }!!.executionList
        assertEquals(size666, s666.size)
        assertEquals(size777, s777.size)
    }

    @Test
    fun testWithServers() {

        runBlocking { executionDao.insert(executionFullList - executionFullList[3]) }
        let {
            val serversWithExecutions = serverDao.activeServerListWithExecutions().await()
            assertNotNull(serversWithExecutions)
            serversWithExecutions!!
            assertEquals(2, serversWithExecutions.size)
            val s666 = serversWithExecutions.find { it.server.id == server666.id }!!.executionList
            val s777 = serversWithExecutions.find { it.server.id == server777.id }!!.executionList
            assertEquals(1, s666.size)
            assertEquals(2, s777.size)
        }

        runBlocking { executionDao.renewal(executionFullList - executionFullList[0]) }
        assertDbExecutions(0, 3)

        runBlocking { executionDao.insert(executionFullList) }
        assertDbExecutions(1, 3)

        runBlocking { executionDao.delete(executionFullList[3]) }
        assertDbExecutions(1, 2)

        runBlocking { executionDao.deleteByServer(server777.id) }
        assertDbExecutions(1, 0)

        runBlocking {
            val alteredList = executionFullList.map {
                Execution(it.id, it.componentExecuted, it.componentFinished, it.componentMapped, it.componentToExecute, it.componentToMap, it.end, it.size, it.start, ExecutionStatus.RUNNING, it.serverId)
            }
            executionDao.silentInsert(alteredList)
        }
        let {
            val serversWithExecutions = serverDao.activeServerListWithExecutions().await()
            serversWithExecutions!!
            val s666 = serversWithExecutions.find { it.server.id == server666.id }!!.executionList
            val s777 = serversWithExecutions.find { it.server.id == server777.id }!!.executionList
            assertEquals(1, s666.size)
            assertEquals(3, s777.size)
            assertTrue(s777.all { it.execution.status == ExecutionStatus.RUNNING })
            assertEquals(ExecutionStatus.FINISHED, s666.first().execution.status)
        }
    }

    @Test
    fun findById() {
        runBlocking { executionDao.insert(executionFullList) }
        assertNull(runBlocking { executionDao.findById("00") })
        val execution = runBlocking { executionDao.findById("0") }
        assertNotNull(execution)
        execution!!
        assertEquals(ExecutionStatus.FINISHED, execution.status)
    }

    @Test
    fun testWithMarks() {
        val list = runBlocking {
            executionDao.insert(executionFullList)
            markDao.markForDeletion("2")
            executionDao.selectDeleted()
        }
        assertEquals(1, list.size)
        assertEquals("2", list.first().id)
    }
}
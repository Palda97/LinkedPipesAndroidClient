package cz.palda97.lpclient.model.repository

import androidx.lifecycle.LiveData
import org.junit.Assert.*
import cz.palda97.lpclient.*
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.db.dao.ExecutionDao
import cz.palda97.lpclient.model.db.dao.MarkForDeletionDao
import cz.palda97.lpclient.model.db.dao.ServerInstanceDao
import cz.palda97.lpclient.model.entities.execution.Execution
import cz.palda97.lpclient.model.entities.execution.ExecutionStatus
import cz.palda97.lpclient.model.entities.execution.ExecutionStatusUtilities
import cz.palda97.lpclient.model.entities.execution.ServerWithExecutions
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.model.network.ExecutionRetrofit.Companion.executionRetrofit
import io.mockk.*
import kotlinx.coroutines.*
import org.junit.*

class ExecutionRepositoryTest
    : TestWithDb() {

    private lateinit var executionDao: ExecutionDao
    private lateinit var serverDao: ServerInstanceDao
    private lateinit var deleteDao: MarkForDeletionDao

    private lateinit var executionRepo: ExecutionRepository
    private lateinit var serverRepo: ServerRepository

    @Before
    fun defineDaosAddServers() {
        executionDao = db.executionDao()
        serverDao = db.serverDao()
        deleteDao = db.markForDeletionDao()

        executionRepo = ExecutionRepository(executionDao, serverDao, deleteDao)
        serverRepo = ServerRepository(serverDao)

        runBlocking {
            SERVER_LIST.forEach {
                serverDao.insertServer(it)
            }
        }
    }

    private lateinit var routines: RepositoryRoutines
    private lateinit var mockExecutionDao: ExecutionDao
    private lateinit var mockServerDao: ServerInstanceDao
    private lateinit var mockDeleteDao: MarkForDeletionDao
    private lateinit var mockRepo: ExecutionRepository
    @Before
    fun mock() {
        routines = mockk()
        every { routines.update(any()) } returns Unit
        mockkObject(Injector)
        every { Injector.repositoryRoutines } returns routines
        every { Injector.serverRepository } returns serverRepo

        mockExecutionDao = mockk(relaxed = true)
        mockServerDao = mockk(relaxed = true)
        mockDeleteDao = mockk(relaxed = true)
        mockRepo = ExecutionRepository(mockExecutionDao, mockServerDao, mockDeleteDao)
    }

    @After
    fun unMock() {
        unmockkAll()
    }

    private val executionList = listOf(
        easyExecution("0", ExecutionStatus.FINISHED, SERVER_LIST.first()),
        easyExecution("1", ExecutionStatus.FINISHED, SERVER_LIST.first()),
        easyExecution("2", ExecutionStatus.FINISHED, SERVER_LIST.last()),
        easyExecution("3", ExecutionStatus.FINISHED, SERVER_LIST.last()),
        easyExecution("4", ExecutionStatus.FINISHED, SERVER_LIST.last())
    )

    private fun LiveData<MailPackage<List<ServerWithExecutions>>>.extract(count: Int = 1) =
        await(count)!!.mailContent!!.flatMap { it.executionList }.map { it.execution }
    
    private fun mockRetrofit(stringFromCall: String? = "", server: ServerInstance? = null)
            = mockRetrofit(stringFromCall, server) { it.executionRetrofit }

    private fun mockAnotherServer(stringFromCall: String? = "", server: ServerInstance? = null)
            = mockRetrofitAddServer(stringFromCall, server) { it.executionRetrofit }

    @Test
    fun cacheDeleteLive() {
        runBlocking { executionDao.insert(executionList) }
        val a = executionRepo.liveExecutions.extract()
        assertListContentMatch(executionList, a)

        val executionToDelete = executionList.first()
        val retrofitB = mockRetrofit("").finishMock { it.delete(executionToDelete.idNumber) }
        runBlocking { executionRepo.deleteRepo.addPending(executionToDelete, 0L) }
        val b = executionRepo.liveExecutions.extract(2)
        assertListContentMatch(executionList - executionToDelete, b)
        verify { retrofitB.delete(executionToDelete.idNumber) }

        val retrofitC0 = mockRetrofit(JUST_CRAB_EXECUTION, SERVER_LIST.first()).finishMock { it.executionList() }
        val retrofitC1 = mockAnotherServer(EDITED_CRAB_EXECUTION, SERVER_LIST.last()).finishMock { it.executionList() }
        runBlocking { executionRepo.cacheExecutions(Either.Right<ServerInstance, List<ServerInstance>?>(SERVER_LIST), false) }
        val c = executionRepo.liveExecutions.extract(2)
        assertListContentMatch(EXPECTED_EXECUTIONS, c)
        coVerify(exactly = 1) {
            retrofitC0.executionList()
            retrofitC1.executionList()
        }
    }

    @Test
    fun serverToFilter() {
        runBlocking { executionDao.insert(executionList) }
        val serverToFilter = SERVER_LIST.last()
        serverRepo.serverToFilter = serverToFilter
        executionRepo.onServerToFilterChange()
        val d = executionRepo.liveExecutions.extract(2)
        assertListContentMatch(executionList.filter { it.serverId == serverToFilter.id }, d)
    }

    @Test
    fun findById() {
        val id = "123"
        runBlocking { mockRepo.find(id) }
        coVerify(exactly = 1) { mockExecutionDao.findById(id) }
    }

    @Test
    fun markTest() {
        val execution = executionList.first()
        runBlocking { mockRepo.markForDeletion(execution) }
        runBlocking { mockRepo.unMarkForDeletion(execution) }
        coVerify(exactly = 1) { mockDeleteDao.markForDeletion(execution.id) }
        coVerify(exactly = 1) { mockDeleteDao.unMarkForDeletion(execution.id) }
    }

    @Test
    fun cleanDb() {
        val retrofit = mockRetrofit().finishMock { it.delete(any()) }
        coEvery { mockExecutionDao.selectDeleted() } returns executionList
        runBlocking { mockRepo.cleanDb() }
        verify(exactly = 1) {
            executionList.forEach {
                retrofit.delete(it.idNumber)
            }
        }
        coVerify(exactly = 1) {
            executionList.forEach {
                mockExecutionDao.delete(it)
                mockDeleteDao.delete(it.id)
            }
        }
    }

    @Test
    fun update() {
        runBlocking { executionDao.insert(executionList) }
        val server = SERVER_LIST.first()
        val rest = SERVER_LIST.last()
        val expected = executionList.filter { it.serverId == rest.id } + EXPECTED_EXECUTIONS.first()
        val retrofit = mockRetrofit(JUST_CRAB_EXECUTION, server).finishMock { it.executionList() }
        runBlocking { executionRepo.update(server) }
        val a = executionRepo.liveExecutions.extract()
        assertListContentMatch(expected, a)
        verify(exactly = 1) { retrofit.executionList() }
    }

    @Test
    fun fetchStatus() {
        val server = SERVER_LIST.first()
        val execution = easyExecution("http://localhost:8080/resources/executions/1618080878407-3-508195fc-fbad-4a2e-bcdd-51de70fed4f1", ExecutionStatus.RUNNING, server)
        val retrofit = mockRetrofit("json").finishMock { it.execution(execution.idNumber) }
        mockkObject(ExecutionStatusUtilities)
        every { ExecutionStatusUtilities.fromDirectRequest("json") } returns ExecutionStatus.FINISHED
        coEvery { mockExecutionDao.findById(execution.id) } returns execution
        coEvery { mockServerDao.findById(server.id) } returns server

        runBlocking { mockRepo.fetchStatus(server.id, execution.id) }

        verify(exactly = 1) { retrofit.execution(execution.idNumber) }
        coVerify(exactly = 1) {
            mockExecutionDao.findById(execution.id)
        }
    }

    companion object {

        private val SERVER_LIST = listOf(
            ServerInstance("server666", "https://example.com").apply { id = 666L },
            ServerInstance("server777", "8.8.8.8").apply { id = 777L }
        )

        private fun easyExecution(id: String, status: ExecutionStatus, server: ServerInstance) =
            Execution(id, 0, 0, 0, 0, 0, null, 0L, null, status, server.id)

        private val EXPECTED_EXECUTIONS = listOf(
            easyExecution("http://localhost:8080/resources/executions/1618080878407-3-508195fc-fbad-4a2e-bcdd-51de70fed4f1", ExecutionStatus.FAILED, SERVER_LIST.first()),
            easyExecution("http://localhost:8080/resources/executions/1618082341988-1-84a09f02-7534-4f0b-a4d0-58ac2ff9368d", ExecutionStatus.FAILED, SERVER_LIST.last())
        )

        private const val JUST_CRAB_EXECUTION = "[{\"@graph\":[{\"@id\":\"http://etl.linkedpipes.com/metadata\",\"@type\":[\"http://etl.linkedpipes.com/ontology/Metadata\"],\"http://etl.linkedpipes.com/ontology/serverTime\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#long\",\"@value\":\"1618080889128\"}]}],\"@id\":\"http://etl.linkedpipes.com/metadata\"},{\"@graph\":[{\"@id\":\"http://localhost:8080/resources/executions/1618080878407-3-508195fc-fbad-4a2e-bcdd-51de70fed4f1\",\"@type\":[\"http://etl.linkedpipes.com/ontology/Execution\"],\"http://etl.linkedpipes.com/ontology/execution/componentExecuted\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#int\",\"@value\":\"0\"}],\"http://etl.linkedpipes.com/ontology/execution/componentFinished\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#int\",\"@value\":\"0\"}],\"http://etl.linkedpipes.com/ontology/execution/componentMapped\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#int\",\"@value\":\"0\"}],\"http://etl.linkedpipes.com/ontology/execution/componentToExecute\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#int\",\"@value\":\"1\"}],\"http://etl.linkedpipes.com/ontology/execution/componentToMap\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#int\",\"@value\":\"0\"}],\"http://etl.linkedpipes.com/ontology/execution/end\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#dateTime\",\"@value\":\"2021-04-10T20:54:39.627+02:00\"}],\"http://etl.linkedpipes.com/ontology/execution/size\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#long\",\"@value\":\"82953\"}],\"http://etl.linkedpipes.com/ontology/execution/start\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#dateTime\",\"@value\":\"2021-04-10T20:54:38.571+02:00\"}],\"http://etl.linkedpipes.com/ontology/pipeline\":[{\"@id\":\"http://localhost:8080/resources/pipelines/1611793236977\"}],\"http://etl.linkedpipes.com/ontology/status\":[{\"@id\":\"http://etl.linkedpipes.com/resources/status/failed\"}]},{\"@id\":\"http://localhost:8080/resources/pipelines/1611793236977\",\"@type\":[\"http://linkedpipes.com/ontology/Pipeline\"],\"http://linkedpipes.com/ontology/executionMetadata\":[{\"@id\":\"http://localhost:8080/resources/pipelines/1611793236977/metadata\"}],\"http://www.w3.org/2004/02/skos/core#prefLabel\":[{\"@value\":\"justCrab\"}]},{\"@id\":\"http://localhost:8080/resources/pipelines/1611793236977/metadata\",\"@type\":[\"http://linkedpipes.com/ontology/ExecutionMetadata\"],\"http://linkedpipes.com/ontology/deleteWorkingData\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#boolean\",\"@value\":\"false\"}],\"http://linkedpipes.com/ontology/execution/type\":[{\"@id\":\"http://linkedpipes.com/resources/executionType/Full\"}],\"http://linkedpipes.com/ontology/logPolicy\":[{\"@id\":\"http://linkedpipes.com/ontology/log/Preserve\"}],\"http://linkedpipes.com/ontology/saveDebugData\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#boolean\",\"@value\":\"true\"}]}],\"@id\":\"http://localhost:8080/resources/executions/1618080878407-3-508195fc-fbad-4a2e-bcdd-51de70fed4f1/list\"}]"
        private const val EDITED_CRAB_EXECUTION = "[{\"@graph\":[{\"@id\":\"http://etl.linkedpipes.com/metadata\",\"@type\":[\"http://etl.linkedpipes.com/ontology/Metadata\"],\"http://etl.linkedpipes.com/ontology/serverTime\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#long\",\"@value\":\"1618082411231\"}]}],\"@id\":\"http://etl.linkedpipes.com/metadata\"},{\"@graph\":[{\"@id\":\"http://localhost:8080/resources/executions/1618082341988-1-84a09f02-7534-4f0b-a4d0-58ac2ff9368d\",\"@type\":[\"http://etl.linkedpipes.com/ontology/Execution\"],\"http://etl.linkedpipes.com/ontology/execution/componentExecuted\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#int\",\"@value\":\"0\"}],\"http://etl.linkedpipes.com/ontology/execution/componentFinished\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#int\",\"@value\":\"0\"}],\"http://etl.linkedpipes.com/ontology/execution/componentMapped\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#int\",\"@value\":\"0\"}],\"http://etl.linkedpipes.com/ontology/execution/componentToExecute\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#int\",\"@value\":\"1\"}],\"http://etl.linkedpipes.com/ontology/execution/componentToMap\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#int\",\"@value\":\"0\"}],\"http://etl.linkedpipes.com/ontology/execution/end\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#dateTime\",\"@value\":\"2021-04-10T21:19:02.156+02:00\"}],\"http://etl.linkedpipes.com/ontology/execution/size\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#long\",\"@value\":\"74733\"}],\"http://etl.linkedpipes.com/ontology/execution/start\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#dateTime\",\"@value\":\"2021-04-10T21:19:02.009+02:00\"}],\"http://etl.linkedpipes.com/ontology/pipeline\":[{\"@id\":\"http://localhost:8080/resources/pipelines/1611796155071\"}],\"http://etl.linkedpipes.com/ontology/status\":[{\"@id\":\"http://etl.linkedpipes.com/resources/status/failed\"}]},{\"@id\":\"http://localhost:8080/resources/pipelines/1611796155071\",\"@type\":[\"http://linkedpipes.com/ontology/Pipeline\"],\"http://linkedpipes.com/ontology/executionMetadata\":[{\"@id\":\"http://localhost:8080/resources/pipelines/1611796155071/metadata\"}],\"http://www.w3.org/2004/02/skos/core#prefLabel\":[{\"@value\":\"editedCrab\"}]},{\"@id\":\"http://localhost:8080/resources/pipelines/1611796155071/metadata\",\"@type\":[\"http://linkedpipes.com/ontology/ExecutionMetadata\"],\"http://linkedpipes.com/ontology/deleteWorkingData\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#boolean\",\"@value\":\"false\"}],\"http://linkedpipes.com/ontology/execution/type\":[{\"@id\":\"http://linkedpipes.com/resources/executionType/Full\"}],\"http://linkedpipes.com/ontology/logPolicy\":[{\"@id\":\"http://linkedpipes.com/ontology/log/Preserve\"}],\"http://linkedpipes.com/ontology/saveDebugData\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#boolean\",\"@value\":\"true\"}]}],\"@id\":\"http://localhost:8080/resources/executions/1618082341988-1-84a09f02-7534-4f0b-a4d0-58ac2ff9368d/list\"}]"
    }
}
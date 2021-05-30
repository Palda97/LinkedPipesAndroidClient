package cz.palda97.lpclient.model.repository

import androidx.lifecycle.LiveData
import org.junit.Assert.*
import cz.palda97.lpclient.*
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.db.dao.MarkForDeletionDao
import cz.palda97.lpclient.model.db.dao.PipelineViewDao
import cz.palda97.lpclient.model.db.dao.ServerInstanceDao
import cz.palda97.lpclient.model.entities.pipelineview.PipelineView
import cz.palda97.lpclient.model.entities.pipelineview.ServerWithPipelineViews
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.model.network.PipelineRetrofit.Companion.pipelineRetrofit
import cz.palda97.lpclient.model.network.RetrofitHelper
import io.mockk.*
import kotlinx.coroutines.*
import org.junit.*

class PipelineViewRepositoryTest
    : TestWithDb() {

    private lateinit var pipelineDao: PipelineViewDao
    private lateinit var serverDao: ServerInstanceDao
    private lateinit var deleteDao: MarkForDeletionDao

    private lateinit var pipelineRepo: PipelineViewRepository
    private lateinit var serverRepo: ServerRepository

    @Before
    fun defineDaosAddServers() {
        pipelineDao = db.pipelineViewDao()
        serverDao = db.serverDao()
        deleteDao = db.markForDeletionDao()

        pipelineRepo = PipelineViewRepository(pipelineDao, serverDao, deleteDao)
        serverRepo = ServerRepository(serverDao)

        runBlocking {
            SERVER_LIST.forEach {
                serverDao.insertServer(it)
            }
        }
    }

    private lateinit var routines: RepositoryRoutines
    private lateinit var mockPipelineDao: PipelineViewDao
    private lateinit var mockServerDao: ServerInstanceDao
    private lateinit var mockDeleteDao: MarkForDeletionDao
    private lateinit var mockRepo: PipelineViewRepository
    @Before
    fun mock() {
        routines = mockk()
        coEvery { routines.update(any()) } returns mockk()
        mockkObject(Injector)
        every { Injector.repositoryRoutines } returns routines
        every { Injector.serverRepository } returns serverRepo

        mockPipelineDao = mockk(relaxed = true)
        mockServerDao = mockk(relaxed = true)
        mockDeleteDao = mockk(relaxed = true)
        mockRepo = PipelineViewRepository(mockPipelineDao, mockServerDao, mockDeleteDao)
    }

    private val pipelineViewList = listOf(
        PipelineView("0", "0", SERVER_LIST.first().id),
        PipelineView("1", "1", SERVER_LIST.first().id),
        PipelineView("2", "2", SERVER_LIST.last().id),
        PipelineView("3", "3", SERVER_LIST.last().id),
        PipelineView("4", "4", SERVER_LIST.last().id)
    )

    private fun LiveData<MailPackage<List<ServerWithPipelineViews>>>.extract(count: Int = 1) =
        await(count)!!.mailContent!!.flatMap { it.pipelineViewList }.map { it.pipelineView }
    
    private fun mockRetrofit(stringFromCall: String? = "", server: ServerInstance? = null)
            = mockRetrofit(stringFromCall, server) { it.pipelineRetrofit }

    @Test
    fun insertDeleteRefresh() {
        val smallList = listOf(
            PipelineView("small0", "0", SERVER_LIST.first().id),
            PipelineView("smallExtra", "extra", SERVER_LIST.first().id)
        )
        runBlocking {
            smallList.forEach {
                pipelineRepo.insertPipelineView(it)
            }
        }
        val a = pipelineRepo.liveServersWithPipelineViews.extract()
        assertListContentMatch(smallList, a)

        val bRetrofit = mockRetrofit().finishMock { it.deletePipeline(smallList.first().idNumber) }
        runBlocking { pipelineRepo.deleteRepo.addPending(smallList.first(), 0) }
        val b = pipelineRepo.liveServersWithPipelineViews.extract(2)
        assertEquals(1, b.size)
        assertEquals(smallList.last(), b.first())

        val cRetrofit = mockRetrofit(PIPELINE_VIEW_LIST).finishMock { it.pipelineList() }
        runBlocking { pipelineRepo.refreshPipelineViews(Either.Left(SERVER_LIST.first())) }
        val c = pipelineRepo.liveServersWithPipelineViews.extract(2)
        assertEquals(2, c.size)
        assertListContentMatch(PIPELINE_VIEW_LIST_EXPECTED, c)

        verify(exactly = 1) {
            bRetrofit.deletePipeline(smallList.first().idNumber)
            cRetrofit.pipelineList()
        }
    }

    @Test
    fun serverToFilter() {
        runBlocking { pipelineDao.insertList(pipelineViewList) }
        val serverToFilter = SERVER_LIST.last()
        serverRepo.serverToFilter = serverToFilter
        pipelineRepo.onServerToFilterChange()
        val d = pipelineRepo.liveServersWithPipelineViews.extract(2).log()
        assertListContentMatch(pipelineViewList.filter { it.serverId == serverToFilter.id }, d)
    }

    @Test
    fun findById() {
        val id = "123"
        runBlocking { mockRepo.findPipelineViewById(id) }
        coVerify(exactly = 1) { mockPipelineDao.findPipelineViewById(id) }
    }

    @Test
    fun markTest() {
        val pipeline = pipelineViewList.first()
        runBlocking { mockRepo.markForDeletion(pipeline) }
        runBlocking { mockRepo.unMarkForDeletion(pipeline) }
        coVerify(exactly = 1) { mockDeleteDao.markForDeletion(pipeline.id) }
        coVerify(exactly = 1) { mockDeleteDao.unMarkForDeletion(pipeline.id) }
    }

    @Test
    fun downloadPipelineString() {
        val pipelineString = "123"

        val pair = mockRetrofit()
        val retrofit = pair.finishMock { it.getPipeline(any()) }
        coEvery { RetrofitHelper.getStringFromCallOrCode(pair.second) } returns Either.Right(pipelineString)

        val pipelineView = PipelineView("HttpGetWithConfig", "http://localhost:8080/resources/pipelines/1612827740647", SERVER_LIST.first().id)
        val response = runBlocking { pipelineRepo.downloadPipelineString(pipelineView) }
        val string = (response as? Either.Right)?.value
        assertEquals(pipelineString, string)

        coVerify(exactly = 1) {
            retrofit.getPipeline(pipelineView.idNumber)
        }
    }

    @Test
    fun cleanDb() {
        val retrofit = mockRetrofit().finishMock { it.deletePipeline(any()) }
        coEvery { mockPipelineDao.selectDeleted() } returns pipelineViewList
        runBlocking { mockRepo.cleanDb() }
        verify(exactly = 1) {
            pipelineViewList.forEach {
                retrofit.deletePipeline(it.idNumber)
            }
        }
        coVerify(exactly = 1) {
            pipelineViewList.forEach {
                mockPipelineDao.deletePipelineView(it)
                mockDeleteDao.delete(it.id)
            }
        }
    }

    @Test
    fun update() {
        runBlocking { pipelineDao.insertList(pipelineViewList) }
        val server = SERVER_LIST.first()
        val rest = SERVER_LIST.last()
        val expected = PIPELINE_VIEW_LIST_EXPECTED + pipelineViewList.filter { it.serverId == rest.id }
        val retrofit = mockRetrofit(PIPELINE_VIEW_LIST, server).finishMock { it.pipelineList() }
        runBlocking { pipelineRepo.update(server) }
        val a = pipelineRepo.liveServersWithPipelineViews.extract()
        assertListContentMatch(expected, a)
        verify(exactly = 1) { retrofit.pipelineList() }
    }

    companion object {

        private val SERVER_LIST = listOf(
            ServerInstance("server666", "https://example.com").apply { id = 666L },
            ServerInstance("server777", "8.8.8.8").apply { id = 777L }
        )
        private val PIPELINE_VIEW_LIST_EXPECTED = listOf(
            PipelineView("LegendaryFoodOntology2", "http://localhost:8080/resources/pipelines/1611789953288", SERVER_LIST.first().id),
            PipelineView("defCrab", "http://localhost:8080/resources/pipelines/1611793092057", SERVER_LIST.first().id)
        )
        private const val PIPELINE_VIEW_LIST = "[\n" +
                "    {\n" +
                "        \"@graph\": [\n" +
                "            {\n" +
                "                \"@id\": \"http://localhost:8080/resources/pipelines/1611789953288\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://linkedpipes.com/ontology/Pipeline\"\n" +
                "                ],\n" +
                "                \"http://www.w3.org/2004/02/skos/core#prefLabel\": [\n" +
                "                    {\n" +
                "                        \"@value\": \"LegendaryFoodOntology2\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ],\n" +
                "        \"@id\": \"http://localhost:8080/resources/pipelines/1611789953288\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"@graph\": [\n" +
                "            {\n" +
                "                \"@id\": \"http://localhost:8080/resources/pipelines/1611793092057\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://linkedpipes.com/ontology/Pipeline\"\n" +
                "                ],\n" +
                "                \"http://www.w3.org/2004/02/skos/core#prefLabel\": [\n" +
                "                    {\n" +
                "                        \"@value\": \"defCrab\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ],\n" +
                "        \"@id\": \"http://localhost:8080/resources/pipelines/1611793092057\"\n" +
                "    }\n" +
                "]"
    }
}
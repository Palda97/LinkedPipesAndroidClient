package cz.palda97.lpclient.model.repository

import android.content.SharedPreferences
import org.junit.Test
import org.junit.Assert.*
import cz.palda97.lpclient.*
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.db.dao.PipelineDao
import cz.palda97.lpclient.model.db.dao.ServerInstanceDao
import cz.palda97.lpclient.model.entities.pipeline.*
import cz.palda97.lpclient.model.entities.pipelineview.PipelineView
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.model.network.PipelineRetrofit.Companion.pipelineRetrofit
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Before

class PipelineRepositoryTest
    : TestWithDb() {

    private lateinit var serverDao: ServerInstanceDao
    private lateinit var pipelineDao: PipelineDao
    private lateinit var sharedPreferences: SharedPreferences
    private var spPipelineServerId = 0L
    private fun sharedPreferencesServer(server: ServerInstance) {
        spPipelineServerId = server.id
    }
    private var spPipelineId: String? = null
    private var spCacheStatus: String? = null
    private lateinit var spEditor: SharedPreferences.Editor
    private lateinit var repo: PipelineRepository
    private lateinit var mockComponentRepo: ComponentRepository

    @Before
    fun setupDb() {
        serverDao = db.serverDao()
        runBlocking { SERVERS.forEach { serverDao.insertServer(it) } }
        pipelineDao = db.pipelineDao()
        mockSharedPreferences()
        sharedPreferencesServer(SERVERS.first())
        repo = PipelineRepository(serverDao, pipelineDao, sharedPreferences)
        mockkObject(Injector)
        mockComponentRepo = mockk() {
            coEvery { cache(any<List<Component>>()) } returns Unit
        }
        every { Injector.componentRepository } returns mockComponentRepo
    }

    @Suppress("LABEL_NAME_CLASH")
    private fun mockSharedPreferences() {
        spPipelineServerId = 0L
        spPipelineId = null
        spCacheStatus = null
        spEditor = mockk() {
            every { putLong(PipelineRepository.PIPELINE_SERVER_ID, any()) } answers {
                spPipelineServerId = secondArg()
                this@mockk
            }
            every { putString(PipelineRepository.PIPELINE_ID, any()) } answers {
                spPipelineId = secondArg()
                this@mockk
            }
            every { putString(PipelineRepository.CACHE_STATUS, any()) } answers {
                spCacheStatus = secondArg()
                this@mockk
            }
            every { apply() } returns Unit
            every { remove(PipelineRepository.CACHE_STATUS) } answers {
                spCacheStatus = null
                this@mockk
            }
        }
        sharedPreferences = mockk() {
            every { getLong(PipelineRepository.PIPELINE_SERVER_ID, 0) } answers { spPipelineServerId }
            every { getString(PipelineRepository.PIPELINE_ID, null) } answers { spPipelineId }
            every { getString(PipelineRepository.CACHE_STATUS, null) } answers { spCacheStatus }
            every { edit() } returns spEditor
        }
    }

    private fun mockRetrofit(stringFromCall: String? = "", server: ServerInstance? = null) =
        mockRetrofit(stringFromCall, server) { it.pipelineRetrofit }

    @Test
    fun savePipeline() {
        fun assertSavedPipeline(pipeline: Pipeline) {
            assertListContentMatch(listOf(pipeline.profile), runBlocking { pipelineDao.getAllProfiles() })
            assertListContentMatch(pipeline.components, runBlocking { pipelineDao.getAllComponents() })
            assertListContentMatch(pipeline.connections, runBlocking { pipelineDao.getAllConnections() })
            assertListContentMatch(pipeline.configurations, runBlocking { pipelineDao.getAllConfiguration() })
            assertListContentMatch(pipeline.vertexes, runBlocking { pipelineDao.getAllVertex() })
            assertListContentMatch(pipeline.templates, runBlocking { pipelineDao.getAllTemplates() })
            assertListContentMatch(pipeline.mapping, runBlocking { pipelineDao.getAllMappings() })
            assertListContentMatch(pipeline.tags, runBlocking { pipelineDao.getAllTags() })
        }
        val job0 = runBlocking { repo.savePipeline(PIPELINE0, false) }
        assertSavedPipeline(PIPELINE0)
        val job1 = runBlocking { repo.savePipeline(PIPELINE1, true) }
        assertSavedPipeline(PIPELINE1)
        assertNull("savePipeline returned job, but it shouldn't", job0)
        assertNotNull("savePipeline didn't returned job, but it should", job1)
        runBlocking { job1!!.join() }
        coVerify(exactly = 1) { mockComponentRepo.cache(PIPELINE1.components) }
        coVerify(exactly = 1) { mockComponentRepo.cache(any<List<Component>>()) }
        coVerify(exactly = 0) { mockComponentRepo.cache(any<Component>()) }
    }

    @Test
    fun cachePipelineInit() {
        every { Injector.pipelineViewRepository } returns mockk() {
            coEvery { downloadPipelineString(PIPELINE_VIEW) } returns Either.Right(EMPTY_PIPELINE_JSON)
        }
        val job = repo.cachePipelineInit(PIPELINE_VIEW)
        runBlocking { job.join() }
        val a = repo.livePipeline.await(2)!!.mailContent!!
        assertEquals(PIPELINE_VIEW, a.pipelineView)
        assertEquals(PIPELINE_VIEW.id, repo.currentPipelineId)
        assertEquals(PIPELINE_VIEW.serverId, repo.currentServerId)
    }

    @Test
    fun retryCachePipeline() {
        val mockPipelineDao: PipelineDao = mockk(relaxed = true)
        val repo = PipelineRepository(serverDao, mockPipelineDao, sharedPreferences)
        runBlocking { repo.retryCachePipeline() }
        val a = repo.livePipeline.await()!!
        assertTrue(a.isError)
        assertEquals(PipelineRepository.CacheStatus.NO_PIPELINE_TO_LOAD.name, a.msg)
        coVerify(exactly = 1) {
            mockPipelineDao.deletePipeline()
        }
        verify(exactly = 1) {
            spEditor.putString(PipelineRepository.CACHE_STATUS, any())
        }

        val mockPipelineViewRepo = mockk<PipelineViewRepository>() {
            coEvery { downloadPipelineString(any()) } returns Either.Right(EMPTY_PIPELINE_JSON)
        }
        every { Injector.pipelineViewRepository } returns mockPipelineViewRepo
        spPipelineId = "p0"
        runBlocking { repo.retryCachePipeline() }
        coVerify(exactly = 1) {
            mockPipelineDao.replacePipeline(any())
            mockPipelineViewRepo.downloadPipelineString(any())
        }
    }

    @Test
    fun liveNewPipeline() {
        val retrofitA = mockRetrofit("").finishMock { it.createPipeline(any()) }
        runBlocking { repo.createPipelineInit(SERVERS.first()).join() }
        val a = (repo.liveNewPipeline.await() as Either.Left).value
        assertEquals(PipelineRepository.CacheStatus.PARSING_ERROR, a)
        verify(exactly = 1) { retrofitA.createPipeline(any()) }
        repo.resetLiveNewPipeline()
        val b = (repo.liveNewPipeline.await() as Either.Left).value
        assertEquals(PipelineRepository.CacheStatus.NO_PIPELINE_TO_LOAD, b)
        val retrofitB = mockRetrofit(NEW_PIPELINE_RESPONSE).finishMock { it.createPipeline(any()) }
        val mockPipelineViewRepo = mockk<PipelineViewRepository>() {
            coEvery { insertPipelineView(any()) } returns Unit
        }
        every { Injector.pipelineViewRepository } returns mockPipelineViewRepo
        runBlocking { repo.createPipelineInit(SERVERS.first()).join() }
        val c = (repo.liveNewPipeline.await() as Either.Right).value
        assertEquals(NEW_PIPELINE_EXPECTED, c)
        verify(exactly = 1) { retrofitB.createPipeline(any()) }
        coVerify(exactly = 1) { mockPipelineViewRepo.insertPipelineView(any()) }
    }

    @Test
    fun createPipelineInit() {
        val pipelineViewRepository = mockk<PipelineViewRepository>() {
            coEvery { insertPipelineView(NEW_PIPELINE_EXPECTED) } returns Unit
        }
        val server = SERVERS.first()
        every { Injector.pipelineViewRepository } returns pipelineViewRepository
        val retrofitA = mockRetrofit(NEW_PIPELINE_RESPONSE, server).finishMock { it.createPipeline(any()) }
        val job = repo.createPipelineInit(server)
        runBlocking { job.join() }
        val a = (repo.liveNewPipeline.await() as Either.Right).value
        assertEquals(NEW_PIPELINE_EXPECTED, a)
        verify { retrofitA.createPipeline(any()) }
        coVerify { pipelineViewRepository.insertPipelineView(NEW_PIPELINE_EXPECTED) }
    }

    @Test
    fun liveUploadStatus() {
        val retrofitA = mockRetrofit("").finishMock { it.updatePipeline(PIPELINE0.pipelineView.idNumber, any()) }
        repo.currentPipelineId = PIPELINE0.pipelineView.id
        repo.currentServerId = SERVERS.first().id
        runBlocking { repo.uploadPipeline() }
        val a = repo.liveUploadStatus.await()
        assertEquals(PipelineRepository.StatusCode.PARSING_ERROR, a)
        runBlocking { pipelineDao.insertPipeline(PIPELINE0) }
        runBlocking { repo.uploadPipeline() }
        val b = repo.liveUploadStatus.await()
        assertEquals(PipelineRepository.StatusCode.OK, b)
        repo.cannotSavePipelineForUpload()
        val c = repo.liveUploadStatus.await()
        assertEquals(PipelineRepository.StatusCode.INTERNAL_ERROR, c)
        repo.resetUploadStatus()
        val d = repo.liveUploadStatus.await()
        assertEquals(PipelineRepository.StatusCode.NEUTRAL, d)
        verify { retrofitA.updatePipeline(PIPELINE0.pipelineView.idNumber, any()) }
    }

    @Test
    fun insertCurrentPipelineView() {
        repo.currentPipelineView = PIPELINE_VIEW
        val pipelineViewRepository = mockk<PipelineViewRepository>() {
            coEvery { insertPipelineView(PIPELINE_VIEW) } returns Unit
        }
        every { Injector.pipelineViewRepository } returns pipelineViewRepository
        runBlocking { repo.insertCurrentPipelineView() }
        coVerify { pipelineViewRepository.insertPipelineView(PIPELINE_VIEW) }
    }

    companion object {

        private fun easyTemplate(id: String) = Template(null, "", "", null, id)
        private fun easyComponent(id: String) = Component(0, 0, easyTemplate(id))
        private fun easyConnection(id: String) = Connection("", "", "", "", emptyList(), id)
        private fun easyConfiguration(id: String) = Configuration(emptyList(), id)
        private fun easyVertex(id: String) = Vertex(0, 0, 0, id)
        private fun easyMapping(id: String) = SameAs(id, "")
        private fun easyTag(id: String) = Tag(id)

        private val SERVERS = listOf(
            ServerInstance("server666", "https://example.com").apply { id = 666L },
            ServerInstance("server777", "8.8.8.8").apply { id = 777L }
        )

        val PIPELINE_VIEW = PipelineView("emptypipeline", "http://localhost:8080/resources/pipelines/1618492981225", SERVERS.first().id)

        private const val EMPTY_PIPELINE_JSON = "[{\"@graph\":[{\"@id\":\"http://localhost:8080/resources/pipelines/1618492981225\",\"@type\":[\"http://linkedpipes.com/ontology/Pipeline\"],\"http://etl.linkedpipes.com/ontology/version\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#int\",\"@value\":\"2\"}],\"http://linkedpipes.com/ontology/profile\":[{\"@id\":\"http://localhost:8080/resources/pipelines/1618492981225/profile/default\"}],\"http://www.w3.org/2004/02/skos/core#prefLabel\":[{\"@value\":\"emptypipeline\"}]},{\"@id\":\"http://localhost:8080/resources/pipelines/1618492981225/profile/default\",\"@type\":[\"http://linkedpipes.com/ontology/ExecutionProfile\"],\"http://linkedpipes.com/ontology/rdfRepositoryPolicy\":[{\"@id\":\"http://linkedpipes.com/ontology/repository/SingleRepository\"}],\"http://linkedpipes.com/ontology/rdfRepositoryType\":[{\"@id\":\"http://linkedpipes.com/ontology/repository/NativeStore\"}]}],\"@id\":\"http://localhost:8080/resources/pipelines/1618492981225\"}]"

        private const val NEW_PIPELINE_RESPONSE = "[{\"@graph\":[{\"@id\":\"http://localhost:8080/resources/pipelines/1618496949951\",\"@type\":[\"http://linkedpipes.com/ontology/Pipeline\"],\"http://www.w3.org/2004/02/skos/core#prefLabel\":[{\"@value\":\"http://localhost:8080/resources/pipelines/1618496949951\"}]}],\"@id\":\"http://localhost:8080/resources/pipelines/1618496949951\"}]"
        private val NEW_PIPELINE_EXPECTED = PipelineView("http://localhost:8080/resources/pipelines/1618496949951", "http://localhost:8080/resources/pipelines/1618496949951", 666)

        private val PIPELINE0 = Pipeline(
            PIPELINE_VIEW,
            Profile(null, null, "p0"),
            listOf(easyComponent("c0"), easyComponent("c1")),
            listOf(easyConnection("con0"), easyConnection("con1")),
            listOf(easyConfiguration("cfg0"), easyConfiguration("cfg1")),
            listOf(easyVertex("v0"), easyVertex("v1")),
            listOf(easyTemplate("t0"), easyTemplate("t1")),
            listOf(easyMapping("m0"), easyMapping("m1")),
            listOf(easyTag("tag0"), easyTag("tag1"))
        )
        private val PIPELINE1 = Pipeline(
            PIPELINE_VIEW,
            Profile(null, null, "p1"),
            listOf(easyComponent("c2"), easyComponent("c3")),
            listOf(easyConnection("con2"), easyConnection("con3")),
            listOf(easyConfiguration("cfg2"), easyConfiguration("cfg3")),
            listOf(easyVertex("v2"), easyVertex("v3")),
            listOf(easyTemplate("t2"), easyTemplate("t3")),
            listOf(easyMapping("m2"), easyMapping("m3")),
            listOf(easyTag("tag2"), easyTag("tag3"))
        )
    }
}
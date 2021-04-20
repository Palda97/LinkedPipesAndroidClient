package cz.palda97.lpclient.model.repository

import android.content.SharedPreferences
import org.junit.Test
import org.junit.Assert.*
import cz.palda97.lpclient.*
import cz.palda97.lpclient.model.db.dao.PipelineDao
import cz.palda97.lpclient.model.db.dao.ServerInstanceDao
import cz.palda97.lpclient.model.entities.pipeline.*
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.model.network.ComponentRetrofit.Companion.componentRetrofit
import cz.palda97.lpclient.model.repository.ComponentRepository.Companion.getRootTemplateId
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Ignore
import retrofit2.Retrofit

class ComponentRepositoryTest
    : TestWithDb() {

    private lateinit var serverDao: ServerInstanceDao
    private lateinit var pipelineDao: PipelineDao
    private lateinit var sharedPreferences: SharedPreferences
    private var sharedPreferencesServerId = 0L
    private fun sharedPreferencesServer(server: ServerInstance) {
            sharedPreferencesServerId = server.id
    }
    private lateinit var repo: ComponentRepository

    @Before
    fun setupDb() {
        serverDao = db.serverDao()
        runBlocking { SERVERS.forEach { serverDao.insertServer(it) } }
        pipelineDao = db.pipelineDao()
        sharedPreferences = mockk()
        every { sharedPreferences.getLong(PipelineRepository.PIPELINE_SERVER_ID, 0) } answers { sharedPreferencesServerId }
        sharedPreferencesServer(SERVERS.first())
        repo = ComponentRepository(serverDao, pipelineDao, sharedPreferences)
    }

    private fun mockBindingFactory(bindings: List<Binding> = emptyList(), json: String? = null) {
        mockkConstructor(BindingFactory::class)
        every { constructedWith<BindingFactory>(
            json?.let { EqMatcher(it) } ?: OfTypeMatcher<String>(String::class)
        ).parse() } returns bindings
    }

    private fun mockRetrofit(stringFromCall: String? = "", server: ServerInstance? = null) =
        mockRetrofit(stringFromCall, server) { it.componentRetrofit }

    @Test
    fun cacheBindings() {
        val key = "key"
        val templateId = "t0"
        val bindings = listOf(Binding(templateId, Binding.Type.CONFIGURATION, "b0", "", "bin0"))
        val retrofitA = mockRetrofit(key).finishMock { it.bindings(templateId) }
        mockBindingFactory(bindings, key)
        runBlocking { repo.cacheBinding(templateId) }
        val a = pipelineDao.liveBindingWithStatus().await()!!
        assertEquals(1, a.size)
        val statusWithBinding = a.first()
        assertEquals(ComponentRepository.StatusCode.OK.name, statusWithBinding.status.result)
        assertListContentMatch(bindings, statusWithBinding.list)
        verify(exactly = 1) { retrofitA.bindings(templateId) }
    }

    @Test
    fun cache() {
        runBlocking { pipelineDao.insertTemplate(TEMPLATES) }
        val retrofitA = mockRetrofit()
            .addMock { it.bindings(BASE_TEMPLATE_ID) }
            .addMock { it.dialog(BASE_TEMPLATE_ID) }
            .finishMock { it.dialogJs(BASE_TEMPLATE_ID) }
        runBlocking { repo.cache(COMPONENT) }
        verify(exactly = 1) {
            retrofitA.bindings(BASE_TEMPLATE_ID)
            retrofitA.dialog(BASE_TEMPLATE_ID)
            retrofitA.dialogJs(BASE_TEMPLATE_ID)
        }
    }

    @Test
    fun setImportantIds() {
        mockkConstructor(BindingRepository::class)
        every { constructedWith<BindingRepository>(OfTypeMatcher<PipelineDao>(PipelineDao::class)).setImportantIds(any(), any()) } returns Unit
        val repo = ComponentRepository(serverDao, pipelineDao, sharedPreferences)
        repo.setImportantIds(COMPONENT, TEMPLATES)
        assertEquals(COMPONENT, repo.currentComponent)
        assertEquals(COMPONENT, repo.configurationRepository.currentComponent)
        assertEquals(COMPONENT, repo.connectionDialogRepository.currentComponent)
        verify(exactly = 1) { repo.bindingRepository.setImportantIds(COMPONENT.id, BASE_TEMPLATE_ID) }
    }

    @Test
    fun updateComponent() {
        val prefLabel = "http get"
        repo.setImportantIds(COMPONENT.copy(), TEMPLATES)
        repo.currentComponent!!.prefLabel = prefLabel
        runBlocking { repo.updateComponent(COMPONENT.id) }
        val a = runBlocking { pipelineDao.findComponentById(COMPONENT.id) }
        assertEquals(COMPONENT.apply { this.prefLabel = prefLabel }, a)
    }

    @Test
    fun deleteCurrentComponent() {
        runBlocking {
            pipelineDao.insertVertex(VERTEXES)
            pipelineDao.insertConnection(CONNECTIONS)
            pipelineDao.insertComponent(COMPONENT)
            pipelineDao.insertComponent(ANOTHER_COMPONENT)
        }
        repo.setImportantIds(COMPONENT, TEMPLATES)
        runBlocking { repo.deleteCurrentComponent() }
        val components = runBlocking { pipelineDao.getAllComponents() }
        val connections = runBlocking { pipelineDao.getAllConnections() }
        val vertexes = runBlocking { pipelineDao.getAllVertex() }
        assertListContentMatch(listOf(ANOTHER_COMPONENT), components)
        assertListContentMatch(CONNECTIONS.filter { it.id == "con2" }, connections)
        assertListContentMatch(VERTEXES.filter { it.id == "v3" }, vertexes)
    }

    @Test
    fun persistConnection() {
        val connection = CONNECTIONS.first()
        val a = runBlocking {
            repo.persistConnection(connection)
            pipelineDao.getAllConnections()
        }
        assertListContentMatch(listOf(connection), a)
    }

    @Test
    fun persistVertexes() {
        val a = runBlocking {
            repo.persistVertexes(VERTEXES)
            pipelineDao.getAllVertex()
        }
        assertListContentMatch(VERTEXES, a)
    }

    @Test
    fun deleteConnection() {
        runBlocking {
            pipelineDao.insertConnection(CONNECTIONS)
            pipelineDao.insertVertex(VERTEXES)
        }
        val connection = CONNECTIONS.first()
        val deletedVertexes = runBlocking { repo.deleteConnection(connection) }
        val restOfConnections = runBlocking { pipelineDao.getAllConnections() }
        val restOfVertexes = runBlocking { pipelineDao.getAllVertex() }
        assertListContentMatch(VERTEXES.filter { it.id == "v0" || it.id == "v1" }, deletedVertexes)
        assertListContentMatch(VERTEXES.filter { it.id != "v0" && it.id != "v1" }, restOfVertexes)
        assertListContentMatch(CONNECTIONS - connection, restOfConnections)
    }

    @Test
    fun getRootTemplateId() {
        runBlocking { pipelineDao.insertTemplate(TEMPLATES) }
        val baseId0 = COMPONENT.getRootTemplateId(TEMPLATES)
        val baseId1 = runBlocking { COMPONENT.getRootTemplateId(pipelineDao) }
        val seven0 = ANOTHER_COMPONENT.getRootTemplateId(TEMPLATES)
        val seven1 = runBlocking { ANOTHER_COMPONENT.getRootTemplateId(pipelineDao) }
        assertEquals(BASE_TEMPLATE_ID, baseId0)
        assertEquals(BASE_TEMPLATE_ID, baseId1)
        assertEquals(ANOTHER_COMPONENT.templateId, seven0)
        assertEquals(ANOTHER_COMPONENT.templateId, seven1)
    }

    companion object {

        private val SERVERS = listOf(
            ServerInstance("server666", "https://example.com").apply { id = 666L },
            ServerInstance("server777", "8.8.8.8").apply { id = 777L }
        )

        private val COMPONENT = Component(null, "t0", 0, 0, "", null, "c0")
        private val ANOTHER_COMPONENT = Component(null, "t777", 0, 0, "", null, "c1")

        private const val BASE_TEMPLATE_ID = "base"
        private val TEMPLATES = listOf(
            Template(null, "t1", "", null, "t0"),
            Template(null, BASE_TEMPLATE_ID, "", null, "t1")
        )

        private val VERTEXES = listOf(
            Vertex(0, 0, 0, "v0"),
            Vertex(0, 0, 0, "v1"),
            Vertex(0, 0, 0, "v2"),
            Vertex(0, 0, 0, "v3")
        )

        private val CONNECTIONS = listOf(
            Connection("", "c0", "", "c1", listOf("v0", "v1"), "con0"),
            Connection("", "c1", "", "c0", listOf("v2"), "con1"),
            Connection("", "c1", "", "c2", listOf("v3"), "con2")
        )
    }
}
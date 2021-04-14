package cz.palda97.lpclient.model.repository

import org.junit.Test
import org.junit.Assert.*
import cz.palda97.lpclient.*
import cz.palda97.lpclient.model.db.dao.PipelineDao
import cz.palda97.lpclient.model.entities.pipeline.*
import cz.palda97.lpclient.viewmodel.editcomponent.BindingComplete
import cz.palda97.lpclient.viewmodel.editcomponent.OnlyStatus
import kotlinx.coroutines.runBlocking
import org.junit.Before

class BindingRepositoryTest
    : TestWithDb() {

    private lateinit var pipelineDao: PipelineDao
    private lateinit var repo: BindingRepository

    @Before
    fun setupDb() {
        pipelineDao = db.pipelineDao()
        repo = BindingRepository(pipelineDao)
        runBlocking {
            pipelineDao.insertComponent(COMPONENTS)
            pipelineDao.insertBinding(BINDINGS)
            pipelineDao.insertConnection(CONNECTIONS)
            pipelineDao.insertStatus(STATUSES_DOWNLOADING)
        }
    }

    @Test
    fun test() {
        var component = COMPONENTS.first()
        repo.setImportantIds(component.id, component.templateId)
        val a0 = repo.liveInputContext.await()
        val a1 = repo.liveOutputContext.await()
        assertEquals(ComponentRepository.StatusCode.DOWNLOAD_IN_PROGRESS, a0?.status)
        assertEquals(ComponentRepository.StatusCode.DOWNLOAD_IN_PROGRESS, a1?.status)
        runBlocking { pipelineDao.insertStatus(STATUSES_OK) }
        val b0 = repo.liveInputContext.await(2) as BindingComplete
        val b1 = repo.liveOutputContext.await(2) as BindingComplete
        with(b0) {
            assertListContentMatch(BINDINGS.filter { it.templateId == component.templateId }, bindings)
            assertListContentMatch(EXPECTED_BINDINGS, otherBindings)
            assertListContentMatch(COMPONENTS, components)
            assertEquals(0, templates.size)
            assertEquals(0, connections.size)
        }
        with(b1) {
            assertListContentMatch(BINDINGS.filter { it.templateId == component.templateId }, bindings)
            assertListContentMatch(EXPECTED_BINDINGS, otherBindings)
            assertListContentMatch(COMPONENTS, components)
            assertEquals(0, templates.size)
            assertListContentMatch(CONNECTIONS, connections)
        }
        component = COMPONENTS.last()
        repo.setImportantIds(component.id, component.templateId)
        val c0 = repo.liveInputContext.await(2) as BindingComplete
        val c1 = repo.liveOutputContext.await(2) as BindingComplete
        with(c0) {
            assertListContentMatch(BINDINGS.filter { it.templateId == component.templateId }, bindings)
            assertListContentMatch(EXPECTED_BINDINGS, otherBindings)
            assertListContentMatch(COMPONENTS, components)
            assertEquals(0, templates.size)
            assertListContentMatch(CONNECTIONS, connections)
        }
        with(c1) {
            assertListContentMatch(BINDINGS.filter { it.templateId == component.templateId }, bindings)
            assertListContentMatch(EXPECTED_BINDINGS, otherBindings)
            assertListContentMatch(COMPONENTS, components)
            assertEquals(0, templates.size)
            assertEquals(0, connections.size)
        }
    }

    companion object {

        private val COMPONENTS = listOf(
            Component(null, "t0", 0, 0, "", null, "c0"),
            Component(null, "t1", 0, 0, "", null, "c1")
        )

        private val BINDINGS = listOf(
            Binding("t0", Binding.Type.CONFIGURATION, "b0", "", "bid0"),
            Binding("t0", Binding.Type.INPUT, "b1", "", "bid1"),
            Binding("t0", Binding.Type.OUTPUT, "b2", "", "bid2"),
            Binding("t1", Binding.Type.INPUT, "b3", "", "bid3"),
            Binding("t1", Binding.Type.OUTPUT, "b4", "", "bid4")
        )

        private val CONNECTIONS = listOf(
            Connection("b2", "c0", "b3", "c1", emptyList(), "con0")
        )

        private val STATUSES_OK = listOf(
            ConfigDownloadStatus("t0", ConfigDownloadStatus.TYPE_BINDING, ComponentRepository.StatusCode.OK),
            ConfigDownloadStatus("t1", ConfigDownloadStatus.TYPE_BINDING, ComponentRepository.StatusCode.OK)
        )

        private val STATUSES_DOWNLOADING = listOf(
            ConfigDownloadStatus("t0", ConfigDownloadStatus.TYPE_BINDING, ComponentRepository.StatusCode.DOWNLOAD_IN_PROGRESS),
            ConfigDownloadStatus("t1", ConfigDownloadStatus.TYPE_BINDING, ComponentRepository.StatusCode.DOWNLOAD_IN_PROGRESS)
        )

        private val EXPECTED_BINDINGS = STATUSES_OK.map {config ->
            StatusWithBinding(config, BINDINGS.filter { it.templateId == config.componentId })
        }
    }
}
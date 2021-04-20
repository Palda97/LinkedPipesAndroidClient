package cz.palda97.lpclient.model.repository

import org.junit.Test
import org.junit.Assert.*
import cz.palda97.lpclient.*
import cz.palda97.lpclient.model.db.dao.PipelineDao
import cz.palda97.lpclient.model.entities.pipeline.Binding
import cz.palda97.lpclient.model.entities.pipeline.Component
import cz.palda97.lpclient.model.entities.pipeline.ConfigDownloadStatus
import cz.palda97.lpclient.model.entities.pipeline.Template
import kotlinx.coroutines.runBlocking
import org.junit.Before

class ConnectionDialogRepositoryTest
    : TestWithDb() {

    private lateinit var pipelineDao: PipelineDao
    private lateinit var repo: ConnectionDialogRepository

    @Before
    fun setup() {
        pipelineDao = db.pipelineDao()
        repo = ConnectionDialogRepository(pipelineDao)

        runBlocking {
            pipelineDao.insertComponent(COMPONENT_LIST)
            pipelineDao.insertTemplate(TEMPLATE_LIST)
            pipelineDao.insertStatus(CONFIG_DOWNLOAD_STATUS)
            pipelineDao.insertBinding(BINDING_LIST)
        }
    }

    @Test
    fun liveComponents() {
        val a = repo.liveComponents.await()!!
        assertListContentMatch(COMPONENT_LIST, a)

        val except = COMPONENT_LIST.first()
        repo.currentComponent = except
        val b = repo.liveComponents.await()!!
        assertListContentMatch(COMPONENT_LIST - except, b)
    }

    @Test
    fun liveBinding() {
        runBlocking { repo.setTemplateForBindings(COMPONENT_LIST.first()) }
        val a = repo.liveBinding.await()!!
        assertEquals(CONFIG_DOWNLOAD_STATUS, a.status)
        assertListContentMatch(BINDING_LIST.filter { it.templateId == BASE_TEMPLATE_ID }, a.list)

        repo.resetTemplateForBindings()
        val b = repo.liveBinding.await()
        assertEquals(0, b?.list?.size)
    }

    @Test
    fun resettingPositions() {
        repo.lastSelectedComponentPosition = 777
        repo.lastSelectedBindingPosition = 777
        repo.resetLastSelected()
        assertEquals(null, repo.lastSelectedComponentPosition)
        assertEquals(null, repo.lastSelectedBindingPosition)
    }

    companion object {

        private fun easyComponent(id: String, templateId: String) = Component(0, 0, easyTemplate(id, templateId))

        private val COMPONENT_LIST = listOf(
            easyComponent("c0", "t0"),
            easyComponent("c1", "t1"),
            easyComponent("c2", "t2"),
            easyComponent("c3", "t3")
        )

        private const val BASE_TEMPLATE_ID = "base"

        private val CONFIG_DOWNLOAD_STATUS = ConfigDownloadStatus(BASE_TEMPLATE_ID, ConfigDownloadStatus.TYPE_BINDING, ComponentRepository.StatusCode.OK)

        private fun easyBinding(id: String, templateId: String, type: Binding.Type) = Binding(templateId, type, "", "", id)

        private val BINDING_LIST = listOf(
            easyBinding("b0", BASE_TEMPLATE_ID, Binding.Type.CONFIGURATION),
            easyBinding("b1", BASE_TEMPLATE_ID, Binding.Type.INPUT),
            easyBinding("b2", BASE_TEMPLATE_ID, Binding.Type.OUTPUT),
            easyBinding("b3", "tt1", Binding.Type.CONFIGURATION),
            easyBinding("b4", "tt1", Binding.Type.OUTPUT)
        )

        private fun easyTemplate(id: String, templateId: String) = Template(null, templateId, "", null, id)

        private val TEMPLATE_LIST = listOf(
            easyTemplate("t0", "tt0"),
            easyTemplate("t1", "tt1"),
            easyTemplate("tt0", BASE_TEMPLATE_ID)
        )
    }
}
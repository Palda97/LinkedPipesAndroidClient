package cz.palda97.lpclient.model.repository

import org.junit.Test
import org.junit.Assert.*
import cz.palda97.lpclient.*
import cz.palda97.lpclient.model.IdGenerator
import cz.palda97.lpclient.model.db.dao.PipelineDao
import cz.palda97.lpclient.model.entities.pipeline.*
import cz.palda97.lpclient.viewmodel.editcomponent.ConfigInputComplete
import cz.palda97.lpclient.viewmodel.editcomponent.OnlyStatus
import kotlinx.coroutines.runBlocking
import org.junit.Before

class ConfigurationRepositoryTest
    : TestWithDb() {

    private lateinit var pipelineDao: PipelineDao
    private lateinit var repo: ConfigurationRepository

    @Before
    fun setupDb() {
        pipelineDao = db.pipelineDao()
        repo = ConfigurationRepository(pipelineDao)
        runBlocking {
            pipelineDao.insertComponent(COMPONENT)
            pipelineDao.insertDialogJs(DIALOG)
            pipelineDao.insertDialogJs(ANOTHER_DIALOG)
            pipelineDao.insertConfiguration(CONFIGURATION)
            pipelineDao.insertConfigInput(CONFIG_INPUT_LIST)
            pipelineDao.insertStatus(CONFIG_STATUS_DOWNLOAD_LIST)
        }
    }

    @Test
    fun test() {
        repo.currentComponent = COMPONENT
        val a = repo.liveConfigInputContext.await()
        assertEquals(OnlyStatus(ComponentRepository.StatusCode.DOWNLOAD_IN_PROGRESS), a)
        runBlocking { pipelineDao.insertStatus(CONFIG_STATUS_OK_LIST) }
        val b = repo.liveConfigInputContext.await(3) as ConfigInputComplete
        assertEquals(ComponentRepository.StatusCode.OK, b.status)
        assertEquals(DIALOG, b.dialogJs)
        assertListContentMatch(CONFIG_INPUT_LIST.filter { it.componentId == COMPONENT_ID }, b.configInputs)

        val hardRedirectFullName = DIALOG.getFullPropertyName("hardRedirect")!!
        val hardRedirectFalse = repo.getString(hardRedirectFullName, DIALOG.configType)
        assertEquals(false.toString(), hardRedirectFalse)
        repo.setString(hardRedirectFullName, true.toString(), DIALOG.configType)
        val hardRedirectTrue = repo.getString(hardRedirectFullName, DIALOG.configType)
        assertEquals(true.toString(), hardRedirectTrue)

        runBlocking { repo.updateConfiguration(COMPONENT_ID) }
        val updatedConfiguration = runBlocking { pipelineDao.findConfigurationByComponentId(COMPONENT_ID) }!!
        val c = updatedConfiguration.getString(hardRedirectFullName, DIALOG.configType)
        assertEquals(true.toString(), c)
    }

    companion object {

        private const val COMPONENT_ID = "c0"

        private const val CONFIGURATION_JSON = PossibleComponentTest.DEFAULT_CONFIGURATION

        private val DIALOG = DialogJs("http://plugins.linkedpipes.com/ontology/e-httpGetFile#", mapOf(
            "fileName" to "fileName",
            "utf8Redirect" to "utf8Redirect",
            "userAgent" to "userAgent",
            "uri" to "fileUri",
            "hardRedirect" to "hardRedirect",
            "encodeUrl" to "encodeUrl"
        ), COMPONENT_ID)
        private val ANOTHER_DIALOG = DialogJs("", mapOf(), "other")

        private fun easyConfigInput(id: String, componentId: String) = ConfigInput("", ConfigInput.Type.EDIT_TEXT, id, componentId)

        private val CONFIG_INPUT_LIST = listOf(
            easyConfigInput("ci0", COMPONENT_ID),
            easyConfigInput("ci1", COMPONENT_ID),
            easyConfigInput("ci2", COMPONENT_ID),
            easyConfigInput("ci3", "other"),
            easyConfigInput("ci4", "other")
        )

        val COMPONENT = Component(IdGenerator.configurationId(COMPONENT_ID), "t0", 0, 0, "", null, COMPONENT_ID)

        val CONFIGURATION = PipelineFactory(null, CONFIGURATION_JSON).parseConfigurationOnly(COMPONENT_ID).mailContent!!

        val CONFIG_STATUS_DOWNLOAD_LIST = listOf(
            ConfigDownloadStatus(COMPONENT_ID, ConfigDownloadStatus.TYPE_DIALOG_JS, ComponentRepository.StatusCode.DOWNLOAD_IN_PROGRESS),
            ConfigDownloadStatus(COMPONENT_ID, ConfigDownloadStatus.TYPE_CONFIG_INPUT, ComponentRepository.StatusCode.DOWNLOAD_IN_PROGRESS)
        )
        val CONFIG_STATUS_OK_LIST = listOf(
            ConfigDownloadStatus(COMPONENT_ID, ConfigDownloadStatus.TYPE_DIALOG_JS, ComponentRepository.StatusCode.OK),
            ConfigDownloadStatus(COMPONENT_ID, ConfigDownloadStatus.TYPE_CONFIG_INPUT, ComponentRepository.StatusCode.OK)
        )
    }
}
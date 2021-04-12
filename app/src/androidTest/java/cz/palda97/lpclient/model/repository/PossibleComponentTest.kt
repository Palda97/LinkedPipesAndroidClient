package cz.palda97.lpclient.model.repository

import org.junit.Test
import org.junit.Assert.*
import cz.palda97.lpclient.*
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.IdGenerator
import cz.palda97.lpclient.model.db.dao.PipelineDao
import cz.palda97.lpclient.model.db.dao.ServerInstanceDao
import cz.palda97.lpclient.model.entities.pipeline.Component
import cz.palda97.lpclient.model.entities.pipeline.Configuration
import cz.palda97.lpclient.model.entities.pipeline.Template
import cz.palda97.lpclient.model.entities.possiblecomponent.PossibleComponent
import cz.palda97.lpclient.model.entities.possiblecomponent.PossibleStatus
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.model.network.PipelineRetrofit.Companion.pipelineRetrofit
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.*
import org.junit.Before

class PossibleComponentTest
    : TestWithDb() {

    private lateinit var serverDao: ServerInstanceDao
    private lateinit var pipelineDao: PipelineDao
    private lateinit var componentRepo: PossibleComponentRepository

    @Before
    fun addServers() {
        serverDao = db.serverDao()
        runBlocking { SERVER_LIST.forEach { serverDao.insertServer(it) } }
        pipelineDao = db.pipelineDao()
        componentRepo = PossibleComponentRepository(serverDao, pipelineDao)
    }

    @RelaxedMockK private lateinit var mockServerDao: ServerInstanceDao
    @RelaxedMockK private lateinit var mockPipelineDao: PipelineDao
    private lateinit var mockRepo: PossibleComponentRepository

    @Before
    fun mock() {
        MockKAnnotations.init(this)
        mockRepo = PossibleComponentRepository(mockServerDao, mockPipelineDao)
    }

    private fun mockRetrofit(stringFromCall: String? = "", server: ServerInstance? = null)
            = mockRetrofit(stringFromCall, server) { it.pipelineRetrofit }
    private fun mockRetrofitAddServer(stringFromCall: String? = "", server: ServerInstance? = null)
            = mockRetrofitAddServer(stringFromCall, server) { it.pipelineRetrofit }

    @Test
    fun cacheLive() {
        val retrofitA = mockRetrofit(LIGHT_LIST, SERVER_LIST.first()).finishMock { it.componentList() }
        val retrofitB = mockRetrofitAddServer("bad json", SERVER_LIST.last()).finishMock { it.componentList() }
        runBlocking { componentRepo.cachePossibleComponents(SERVER_LIST) }
        componentRepo.currentServerId = SERVER_LIST.first().id
        val comboA = componentRepo.liveComponents.await()!!
        assertEquals(PossibleStatus(SERVER_LIST.first().id, PossibleComponentRepository.StatusCode.OK), comboA.status)
        assertEquals(EXPECTED_LIGHT_LIST, comboA.list)
        componentRepo.currentServerId = SERVER_LIST.last().id
        val comboB = componentRepo.liveComponents.await()!!
        assertEquals(PossibleStatus(SERVER_LIST.last().id, PossibleComponentRepository.StatusCode.PARSING_ERROR), comboB.status)
        assertEquals(emptyList<PossibleComponent>(), comboB.list)
        verify(exactly = 1) {
            retrofitA.componentList()
            retrofitB.componentList()
        }
    }

    @Test
    fun defaultConfiguration() {
        val component = EXPECTED_LIGHT_LIST.first()
        val retrofitA = mockRetrofit(DEFAULT_CONFIGURATION, SERVER_LIST.first()).finishMock { it.componentDefaultConfiguration(component.id) }
        componentRepo.currentServerId = SERVER_LIST.first().id
        val newComponentId = "123"
        val configurationA = when(val res = runBlocking { componentRepo.downloadDefaultConfiguration(component, newComponentId) }) {
            is Either.Left -> assertFail("Instead of configuration, we got: ${res.value}")
            is Either.Right -> res.value
        }//.log { it.newLineInsteadOfComma() }
        assertEquals(IdGenerator.configurationId(newComponentId), configurationA.id)
        assertEquals(10, configurationA.settings.first().settings.size)

        val retrofitB = mockRetrofit("bad json", SERVER_LIST.first()).finishMock { it.componentDefaultConfiguration(component.id) }
        val status = when(val res = runBlocking { componentRepo.downloadDefaultConfiguration(component, newComponentId) }) {
            is Either.Left -> res.value
            is Either.Right -> assertFail("There should be a parsing error.")
        }
        assertEquals(PossibleComponentRepository.StatusCode.PARSING_ERROR, status)

        verify(exactly = 1) {
            retrofitA.componentDefaultConfiguration(component.id)
            retrofitB.componentDefaultConfiguration(component.id)
        }
    }

    @Test
    fun persist() {
        val templateList: List<Template> = mockk()
        val component: Component = mockk()
        val configuration: Configuration = mockk()
        val configurationList: List<Configuration> = mockk()
        runBlocking {
            mockRepo.persistTemplate(templateList)
            mockRepo.persistComponent(component)
            mockRepo.persistConfiguration(configuration)
            mockRepo.persistConfiguration(configurationList)
        }
        coVerify(exactly = 1) {
            mockPipelineDao.insertTemplate(templateList)
            mockPipelineDao.insertComponent(component)
            mockPipelineDao.insertConfiguration(configuration)
            mockPipelineDao.insertConfiguration(configurationList)
        }
    }

    @Test
    fun getTemplatesAndConfigurations() {
        runBlocking { pipelineDao.insertPossibleComponent(EXPECTED_LIGHT_LIST) }
        val template = EXPECTED_LIGHT_LIST.first()
        val component = EXPECTED_LIGHT_LIST.last()
        val retrofitA = mockRetrofit(DEFAULT_CONFIGURATION, SERVER_LIST.first()).finishMock { it.templateConfiguration(component.id) }
        componentRepo.currentServerId = SERVER_LIST.first().id
        val a = when(val res = runBlocking { componentRepo.getTemplatesAndConfigurations(component) }) {
            is Either.Left -> assertFail("Instead of configuration, we got: ${res.value}")
            is Either.Right -> res.value
        }//.log { it.newLineInsteadOfComma() }
        assertEquals(1, a.first.size)
        assertEquals(template.id, a.second)
        verify {
            retrofitA.templateConfiguration(component.id)
        }
    }

    companion object {

        private val SERVER_LIST = listOf(
            ServerInstance("server666", "https://example.com").apply { id = 666L },
            ServerInstance("server777", "8.8.8.8").apply { id = 777L }
        )

        val EXPECTED_LIGHT_LIST = listOf(
            PossibleComponent("http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0", SERVER_LIST.first().id, "HTTP get", null),
            PossibleComponent("http://localhost:8080/resources/components/1605020230097-2a8a2ab0-f348-4c34-aece-fb67db1836c2", SERVER_LIST.first().id, "download crab", "http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0")
        )

        private const val LIGHT_LIST = "[\n" +
                "    {\n" +
                "        \"@graph\": [\n" +
                "            {\n" +
                "                \"@id\": \"http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://linkedpipes.com/ontology/JarTemplate\"\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/color\": [\n" +
                "                    {\n" +
                "                        \"@value\": \"#F6D8CE\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/componentType\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://etl.linkedpipes.com/ontology/component/type/Extractor\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/configurationDescription\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://linkedpipes.com/resources/components/e-httpGetFile/0.0.0/configuration/desc\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/dialog\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0/dialog/template\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"@id\": \"http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0/dialog/instance\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"@id\": \"http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0/dialog/config\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/infoLink\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"https://etl.linkedpipes.com/components/e-httpgetfile\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/jar\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://etl.linkedpipes.com/resources/jars/e-httpGetFile/0.0.0\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/keyword\": [\n" +
                "                    {\n" +
                "                        \"@value\": \"HTTP\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"@value\": \"get\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/port\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0/output\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"@id\": \"http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0/configuration\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/requirement\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://linkedpipes.com/resources/requirement/workingDirectory\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/supportControl\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#boolean\",\n" +
                "                        \"@value\": \"true\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://www.w3.org/2004/02/skos/core#prefLabel\": [\n" +
                "                    {\n" +
                "                        \"@value\": \"HTTP get\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"@id\": \"http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0/configuration\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://linkedpipes.com/ontology/dataUnit/sesame/1.0/rdf/SingleGraph\",\n" +
                "                    \"http://linkedpipes.com/ontology/RuntimeConfiguration\"\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/binding\": [\n" +
                "                    {\n" +
                "                        \"@value\": \"Configuration\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://www.w3.org/2004/02/skos/core#prefLabel\": [\n" +
                "                    {\n" +
                "                        \"@value\": \"Configuration\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"@id\": \"http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0/dialog/config\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://linkedpipes.com/ontology/Dialog\"\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/name\": [\n" +
                "                    {\n" +
                "                        \"@value\": \"config\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"@id\": \"http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0/dialog/instance\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://linkedpipes.com/ontology/Dialog\"\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/name\": [\n" +
                "                    {\n" +
                "                        \"@value\": \"instance\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"@id\": \"http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0/dialog/template\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://linkedpipes.com/ontology/Dialog\"\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/name\": [\n" +
                "                    {\n" +
                "                        \"@value\": \"template\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"@id\": \"http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0/output\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://linkedpipes.com/ontology/dataUnit/system/1.0/files/DirectoryMirror\",\n" +
                "                    \"http://linkedpipes.com/ontology/Output\"\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/binding\": [\n" +
                "                    {\n" +
                "                        \"@value\": \"FilesOutput\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/requirement\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://linkedpipes.com/resources/requirement/workingDirectory\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://www.w3.org/2004/02/skos/core#prefLabel\": [\n" +
                "                    {\n" +
                "                        \"@value\": \"Output\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ],\n" +
                "        \"@id\": \"http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"@graph\": [\n" +
                "            {\n" +
                "                \"@id\": \"http://localhost:8080/resources/components/1605020230097-2a8a2ab0-f348-4c34-aece-fb67db1836c2\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://linkedpipes.com/ontology/Template\"\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/configurationGraph\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://localhost:8080/resources/components/1605020230097-2a8a2ab0-f348-4c34-aece-fb67db1836c2/configuration\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/template\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://www.w3.org/2004/02/skos/core#prefLabel\": [\n" +
                "                    {\n" +
                "                        \"@value\": \"download crab\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ],\n" +
                "        \"@id\": \"http://localhost:8080/resources/components/1605020230097-2a8a2ab0-f348-4c34-aece-fb67db1836c2\"\n" +
                "    }\n" +
                "]"

        private const val DEFAULT_CONFIGURATION = "[\n" +
                "    {\n" +
                "        \"@graph\": [\n" +
                "            {\n" +
                "                \"@id\": \"http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0/new/1\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#Configuration\"\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#encodeUrlControl\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileName\": [\n" +
                "                    {\n" +
                "                        \"@value\": \"\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileNameControl\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileUri\": [\n" +
                "                    {\n" +
                "                        \"@value\": \"\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileUriControl\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#hardRedirect\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#boolean\",\n" +
                "                        \"@value\": \"false\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#hardRedirectControl\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#userAgent\": [\n" +
                "                    {\n" +
                "                        \"@value\": \"\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#userAgentControl\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#utf8RedirectControl\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ],\n" +
                "        \"@id\": \"http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0/new\"\n" +
                "    }\n" +
                "]"
    }
}
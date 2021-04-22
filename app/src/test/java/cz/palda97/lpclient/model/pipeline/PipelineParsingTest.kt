package cz.palda97.lpclient.model.pipeline

import cz.palda97.lpclient.*
import cz.palda97.lpclient.model.entities.pipeline.PipelineFactory
import cz.palda97.lpclient.model.entities.server.ServerInstance
import org.junit.Test

import org.junit.Assert.*

class PipelineParsingTest
    : MockkTest() {

    @Test
    fun badJson() {
        val json = "bad json"
        val mail = PipelineFactory(server, json).parse()
        assertTrue(mail.isError)
    }

    @Test
    fun vertexes() {
        val json = stringFromFile("vertexTest.jsonld")
        val pipeline = PipelineFactory(server, json).parse().mailContent
        assertNotNull(pipeline)
        pipeline!!
        assertEquals(6, pipeline.vertexes.size)
        assertEquals(6, pipeline.connections.flatMap { it.vertexIds }.size)
        val vertex = pipeline.vertexes.find { it.id == "http://localhost:8080/resources/pipelines/1617549545338/vertex/0fb3-8270" }
        assertNotNull("Vertex with this id is not present.", vertex)
        vertex!!
        assertEquals(3, vertex.order)
        assertEquals(652, vertex.x)
        assertEquals(250, vertex.y)
    }

    @Test
    fun parseSameAsTagVertexesNullProfile() {
        val json = stringFromFile("sameAsTagNullProfile.jsonld")
        val mail = PipelineFactory(server, json).parse()
        assertTrue("Mail is not ok!", mail.isOk)
        val pipeline = mail.mailContent!!
        assertEquals(1, pipeline.mapping.size)
        assertEquals("https://fit1.opendata.cz/resources/components/1490269248292", pipeline.mapping.first().id)
        assertEquals("https://demo.etl.linkedpipes.com/resources/components/1519816576399", pipeline.mapping.first().sameAs)
        assertEquals(1, pipeline.tags.size)
        val tag = pipeline.tags.first()
        assertEquals("Brno", tag.value)
        assertEquals(tag.value, tag.toString())
    }

    @Test
    fun parseNullConfiguration() {
        val json = stringFromFile("nullConfigurationId.jsonld")
        val mail = PipelineFactory(server, json).parse()
        assertTrue("Mail is not ok!", mail.isOk)
    }

    @Test
    fun parseConfiguration() {
        val mail = PipelineFactory(server, TABULAR_CONFIGURATION).parseConfigurationOnly()
        assertTrue("Mail is not ok!", mail.isOk)
        val configuration = mail.mailContent!!
        assertEquals(3, configuration.settings.size)

        val id = "http://plugins.linkedpipes.com/ontology/t-tabular#fullMapping"
        val type = "http://www.w3.org/ns/csvw#Table"
        assertEquals("true", configuration.getString(id, type))
        configuration.setString(id, "false", type)
        assertEquals("false", configuration.getString(id, type))


    }

    @Test
    fun parseAbc() {
        val mail = PipelineFactory(server, ABC_PIPELINE).parse()
        assertTrue("Mail is not ok!", mail.isOk)
        val pipeline = mail.mailContent!!
        assertEquals(2, pipeline.components.size)
        assertEquals(1, pipeline.connections.size)
        assertEquals(2, pipeline.configurations.size)

        assertEquals("1604082676059", pipeline.pipelineView.idNumber)
        assertEquals(server.id, pipeline.pipelineView.serverId)
    }

    @Test
    fun parseCrab() {
        val mail = PipelineFactory(server, CRAB_PIPELINE).parse()
        assertTrue("Mail is not ok!", mail.isOk)
        val pipeline = mail.mailContent!!
        assertEquals(1, pipeline.components.size)
        assertEquals(0, pipeline.connections.size)
        assertEquals(1, pipeline.configurations.size)
    }

    companion object {

        val server = ServerInstance("Home Server", "http://localhost:8080/")

        const val TABULAR_CONFIGURATION = "[\n" +
                "    {\n" +
                "        \"@graph\": [\n" +
                "            {\n" +
                "                \"@id\": \"http://etl.linkedpipes.com/resources/components/t-tabular/0.0.0/new/1\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://www.w3.org/ns/csvw#Schema\"\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"@id\": \"http://etl.linkedpipes.com/resources/components/t-tabular/0.0.0/new/2\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://www.w3.org/ns/csvw#Table\"\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/t-tabular#baseUriControl\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/t-tabular#dialectControl\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/t-tabular#encodeTypeControl\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/t-tabular#fullMapping\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#boolean\",\n" +
                "                        \"@value\": \"true\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/t-tabular#fullMappingControl\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/t-tabular#generateNullHeaderNamesControl\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/t-tabular#normalOutput\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#boolean\",\n" +
                "                        \"@value\": \"false\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/t-tabular#normalOutputControl\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/t-tabular#rowLimitControl\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/t-tabular#skipLines\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#integer\",\n" +
                "                        \"@value\": \"0\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/t-tabular#skipLinesControl\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/t-tabular#skipOnErrorControl\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/t-tabular#tableSchemaControl\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/t-tabular#useBaseUriControl\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://www.w3.org/ns/csvw#dialect\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://etl.linkedpipes.com/resources/components/t-tabular/0.0.0/new/3\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://www.w3.org/ns/csvw#tableSchema\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://etl.linkedpipes.com/resources/components/t-tabular/0.0.0/new/1\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"@id\": \"http://etl.linkedpipes.com/resources/components/t-tabular/0.0.0/new/3\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://www.w3.org/ns/csvw#Dialect\"\n" +
                "                ],\n" +
                "                \"http://www.w3.org/ns/csvw#delimeter\": [\n" +
                "                    {\n" +
                "                        \"@value\": \",\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://www.w3.org/ns/csvw#encoding\": [\n" +
                "                    {\n" +
                "                        \"@value\": \"UTF-8\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://www.w3.org/ns/csvw#header\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#boolean\",\n" +
                "                        \"@value\": \"true\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://www.w3.org/ns/csvw#quoteChar\": [\n" +
                "                    {\n" +
                "                        \"@value\": \"\\\"\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://www.w3.org/ns/csvw#trim\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#boolean\",\n" +
                "                        \"@value\": \"false\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ],\n" +
                "        \"@id\": \"http://etl.linkedpipes.com/resources/components/t-tabular/0.0.0/new\"\n" +
                "    }\n" +
                "]"

        const val ABC_PIPELINE = "[\n" +
                "    {\n" +
                "        \"@graph\": [\n" +
                "            {\n" +
                "                \"@id\": \"http://localhost:8080/resources/pipelines/1604082676059\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://linkedpipes.com/ontology/Pipeline\"\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/version\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#int\",\n" +
                "                        \"@value\": \"2\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/profile\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://localhost:8080/resources/pipelines/1604082676059/profile/default\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://www.w3.org/2004/02/skos/core#prefLabel\": [\n" +
                "                    {\n" +
                "                        \"@value\": \"abc\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"@id\": \"http://localhost:8080/resources/pipelines/1604082676059/component/a0db-a8d9\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://linkedpipes.com/ontology/Component\"\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/configurationGraph\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://localhost:8080/resources/pipelines/1604082676059/component/a0db-a8d9/configuration\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/template\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/x\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#integer\",\n" +
                "                        \"@value\": \"255\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/y\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#integer\",\n" +
                "                        \"@value\": \"92\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://www.w3.org/2004/02/skos/core#prefLabel\": [\n" +
                "                    {\n" +
                "                        \"@value\": \"Get me abc\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"@id\": \"http://localhost:8080/resources/pipelines/1604082676059/component/b3a5-8b12\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://linkedpipes.com/ontology/Component\"\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/configurationGraph\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://localhost:8080/resources/pipelines/1604082676059/component/b3a5-8b12/configuration\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/template\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://etl.linkedpipes.com/resources/components/t-packZip/0.0.0\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/x\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#integer\",\n" +
                "                        \"@value\": \"493\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/y\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#integer\",\n" +
                "                        \"@value\": \"90\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://www.w3.org/2004/02/skos/core#prefLabel\": [\n" +
                "                    {\n" +
                "                        \"@value\": \"Create zip archive\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"@id\": \"http://localhost:8080/resources/pipelines/1604082676059/connection/2edc-b14a\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://linkedpipes.com/ontology/Connection\"\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/sourceBinding\": [\n" +
                "                    {\n" +
                "                        \"@value\": \"FilesOutput\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/sourceComponent\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://localhost:8080/resources/pipelines/1604082676059/component/a0db-a8d9\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/targetBinding\": [\n" +
                "                    {\n" +
                "                        \"@value\": \"FilesInput\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/targetComponent\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://localhost:8080/resources/pipelines/1604082676059/component/b3a5-8b12\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"@id\": \"http://localhost:8080/resources/pipelines/1604082676059/profile/default\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://linkedpipes.com/ontology/ExecutionProfile\"\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/rdfRepositoryPolicy\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://linkedpipes.com/ontology/repository/SingleRepository\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/rdfRepositoryType\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://linkedpipes.com/ontology/repository/NativeStore\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ],\n" +
                "        \"@id\": \"http://localhost:8080/resources/pipelines/1604082676059\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"@graph\": [\n" +
                "            {\n" +
                "                \"@id\": \"http://localhost:8080/resources/pipelines/1604082676059/component/a0db-a8d9/configuration\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#Configuration\"\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#encodeUrl\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#boolean\",\n" +
                "                        \"@value\": \"false\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#encodeUrlControl\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileName\": [\n" +
                "                    {\n" +
                "                        \"@value\": \"abc.txt\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileNameControl\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileUri\": [\n" +
                "                    {\n" +
                "                        \"@value\": \"http://localhost:1234/abc.txt\"\n" +
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
                "                \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#utf8Redirect\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#boolean\",\n" +
                "                        \"@value\": \"false\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#utf8RedirectControl\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ],\n" +
                "        \"@id\": \"http://localhost:8080/resources/pipelines/1604082676059/component/a0db-a8d9/configuration\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"@graph\": [\n" +
                "            {\n" +
                "                \"@id\": \"http://localhost:8080/resources/pipelines/1604082676059/component/b3a5-8b12/configuration\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://plugins.linkedpipes.com/ontology/t-packZip#Configuration\"\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/t-packZip#fileName\": [\n" +
                "                    {\n" +
                "                        \"@value\": \"output.ttl\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/t-packZip#fileNameControl\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ],\n" +
                "        \"@id\": \"http://localhost:8080/resources/pipelines/1604082676059/component/b3a5-8b12/configuration\"\n" +
                "    }\n" +
                "]"

        const val CRAB_PIPELINE = "[\n" +
                "    {\n" +
                "        \"@graph\": [\n" +
                "            {\n" +
                "                \"@id\": \"http://localhost:8080/resources/pipelines/1600968747469\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://linkedpipes.com/ontology/Pipeline\"\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/version\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#int\",\n" +
                "                        \"@value\": \"2\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/profile\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://localhost:8080/resources/pipelines/1600968747469/profile/default\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://www.w3.org/2004/02/skos/core#prefLabel\": [\n" +
                "                    {\n" +
                "                        \"@value\": \"Crab \uD83E\uDD80\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"@id\": \"http://localhost:8080/resources/pipelines/1600968747469/component/9b63-878f\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://linkedpipes.com/ontology/Component\"\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/configurationGraph\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://localhost:8080/resources/pipelines/1600968747469/component/9b63-878f/configuration\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/template\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/x\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#integer\",\n" +
                "                        \"@value\": \"111\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/y\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#integer\",\n" +
                "                        \"@value\": \"68\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://www.w3.org/2004/02/skos/core#prefLabel\": [\n" +
                "                    {\n" +
                "                        \"@value\": \"HTTP get\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"@id\": \"http://localhost:8080/resources/pipelines/1600968747469/profile/default\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://linkedpipes.com/ontology/ExecutionProfile\"\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/rdfRepositoryPolicy\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://linkedpipes.com/ontology/repository/SingleRepository\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/rdfRepositoryType\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://linkedpipes.com/ontology/repository/NativeStore\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ],\n" +
                "        \"@id\": \"http://localhost:8080/resources/pipelines/1600968747469\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"@graph\": [\n" +
                "            {\n" +
                "                \"@id\": \"http://localhost:8080/resources/pipelines/1600968747469/component/9b63-878f/configuration\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#Configuration\"\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#encodeUrl\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#boolean\",\n" +
                "                        \"@value\": \"false\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#encodeUrlControl\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileName\": [\n" +
                "                    {\n" +
                "                        \"@value\": \"fake_file.txt\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileNameControl\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileUri\": [\n" +
                "                    {\n" +
                "                        \"@value\": \"http://localhost:1234/fake_file.txt\"\n" +
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
                "                \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#utf8Redirect\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#boolean\",\n" +
                "                        \"@value\": \"false\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#utf8RedirectControl\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ],\n" +
                "        \"@id\": \"http://localhost:8080/resources/pipelines/1600968747469/component/9b63-878f/configuration\"\n" +
                "    }\n" +
                "]"

    }
}
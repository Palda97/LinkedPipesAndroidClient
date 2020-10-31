package cz.palda97.lpclient.model.pipeline

import cz.palda97.lpclient.*
import cz.palda97.lpclient.model.entities.pipeline.PipelineFactory
import cz.palda97.lpclient.model.entities.server.ServerInstance
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class PipelineParsingTest {

    @Test
    fun parseAbc() {
        val mail = PipelineFactory(server, ABC_PIPELINE).pipeline
        assertTrue("Mail is not ok!", mail.isOk)
        val pipeline = mail.mailContent!!
        assertEquals(2, pipeline.components.size)
        assertEquals(1, pipeline.connections.size)
        assertEquals(2, pipeline.configurations.size)
    }

    @Test
    fun parseCrab() {
        val mail = PipelineFactory(server, CRAB_PIPELINE).pipeline
        assertTrue("Mail is not ok!", mail.isOk)
        val pipeline = mail.mailContent!!
        assertEquals(1, pipeline.components.size)
        assertEquals(0, pipeline.connections.size)
        assertEquals(1, pipeline.configurations.size)
    }

    companion object {

        val server = ServerInstance("Home Server", "http://localhost:8080/")

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
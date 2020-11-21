package cz.palda97.lpclient.model.pipeline

import cz.palda97.lpclient.*
import cz.palda97.lpclient.model.entities.pipeline.PipelineFactory
import cz.palda97.lpclient.model.entities.pipeline.jsonLd
import cz.palda97.lpclient.model.entities.server.ServerInstance
import org.junit.Test

import org.junit.Assert.*

class JsonLdGeneratorTest {

    @Test
    fun parseAbc() {
        val source = ABC
        val pipeline = PipelineFactory(server, source).pipeline.mailContent!!
        val jsonLd = pipeline.jsonLd()
        assertEquals(source, jsonLd)
    }

    @Test
    fun parseEverything() {
        val source = EVERYTHING
        val expectedPipeline = PipelineFactory(server, source).pipeline.mailContent!!
        val testPipeline = PipelineFactory(server, expectedPipeline.jsonLd()).pipeline.mailContent
        assertNotNull("testPipeline parse error", testPipeline)
        assertEquals(expectedPipeline, testPipeline)
    }

    companion object {

        val server = ServerInstance("Home Server", "http://localhost:8080/")

        const val ABC = "[{\"@graph\":[{\"@id\":\"http://localhost:8080/resources/pipelines/1604082676059\",\"@type\":[\"http://linkedpipes.com/ontology/Pipeline\"],\"http://etl.linkedpipes.com/ontology/version\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#int\",\"@value\":\"2\"}],\"http://linkedpipes.com/ontology/profile\":[{\"@id\":\"http://localhost:8080/resources/pipelines/1604082676059/profile/default\"}],\"http://www.w3.org/2004/02/skos/core#prefLabel\":[{\"@value\":\"abc\"}]},{\"@id\":\"http://localhost:8080/resources/pipelines/1604082676059/component/a0db-a8d9\",\"@type\":[\"http://linkedpipes.com/ontology/Component\"],\"http://linkedpipes.com/ontology/configurationGraph\":[{\"@id\":\"http://localhost:8080/resources/pipelines/1604082676059/component/a0db-a8d9/configuration\"}],\"http://linkedpipes.com/ontology/template\":[{\"@id\":\"http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0\"}],\"http://linkedpipes.com/ontology/x\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#integer\",\"@value\":\"255\"}],\"http://linkedpipes.com/ontology/y\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#integer\",\"@value\":\"92\"}],\"http://www.w3.org/2004/02/skos/core#prefLabel\":[{\"@value\":\"Getmeabc\"}]},{\"@id\":\"http://localhost:8080/resources/pipelines/1604082676059/component/b3a5-8b12\",\"@type\":[\"http://linkedpipes.com/ontology/Component\"],\"http://linkedpipes.com/ontology/configurationGraph\":[{\"@id\":\"http://localhost:8080/resources/pipelines/1604082676059/component/b3a5-8b12/configuration\"}],\"http://linkedpipes.com/ontology/template\":[{\"@id\":\"http://etl.linkedpipes.com/resources/components/t-packZip/0.0.0\"}],\"http://linkedpipes.com/ontology/x\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#integer\",\"@value\":\"493\"}],\"http://linkedpipes.com/ontology/y\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#integer\",\"@value\":\"90\"}],\"http://www.w3.org/2004/02/skos/core#prefLabel\":[{\"@value\":\"Createziparchive\"}]},{\"@id\":\"http://localhost:8080/resources/pipelines/1604082676059/connection/2edc-b14a\",\"@type\":[\"http://linkedpipes.com/ontology/Connection\"],\"http://linkedpipes.com/ontology/sourceBinding\":[{\"@value\":\"FilesOutput\"}],\"http://linkedpipes.com/ontology/sourceComponent\":[{\"@id\":\"http://localhost:8080/resources/pipelines/1604082676059/component/a0db-a8d9\"}],\"http://linkedpipes.com/ontology/targetBinding\":[{\"@value\":\"FilesInput\"}],\"http://linkedpipes.com/ontology/targetComponent\":[{\"@id\":\"http://localhost:8080/resources/pipelines/1604082676059/component/b3a5-8b12\"}]},{\"@id\":\"http://localhost:8080/resources/pipelines/1604082676059/profile/default\",\"@type\":[\"http://linkedpipes.com/ontology/ExecutionProfile\"],\"http://linkedpipes.com/ontology/rdfRepositoryPolicy\":[{\"@id\":\"http://linkedpipes.com/ontology/repository/SingleRepository\"}],\"http://linkedpipes.com/ontology/rdfRepositoryType\":[{\"@id\":\"http://linkedpipes.com/ontology/repository/NativeStore\"}]}],\"@id\":\"http://localhost:8080/resources/pipelines/1604082676059\"},{\"@graph\":[{\"@id\":\"http://localhost:8080/resources/pipelines/1604082676059/component/a0db-a8d9/configuration\",\"@type\":[\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#Configuration\"],\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#encodeUrl\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#boolean\",\"@value\":\"false\"}],\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#encodeUrlControl\":[{\"@id\":\"http://plugins.linkedpipes.com/resource/configuration/None\"}],\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileName\":[{\"@value\":\"abc.txt\"}],\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileNameControl\":[{\"@id\":\"http://plugins.linkedpipes.com/resource/configuration/None\"}],\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileUri\":[{\"@value\":\"http://localhost:1234/abc.txt\"}],\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileUriControl\":[{\"@id\":\"http://plugins.linkedpipes.com/resource/configuration/None\"}],\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#hardRedirect\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#boolean\",\"@value\":\"false\"}],\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#hardRedirectControl\":[{\"@id\":\"http://plugins.linkedpipes.com/resource/configuration/None\"}],\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#userAgent\":[{\"@value\":\"\"}],\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#userAgentControl\":[{\"@id\":\"http://plugins.linkedpipes.com/resource/configuration/None\"}],\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#utf8Redirect\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#boolean\",\"@value\":\"false\"}],\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#utf8RedirectControl\":[{\"@id\":\"http://plugins.linkedpipes.com/resource/configuration/None\"}]}],\"@id\":\"http://localhost:8080/resources/pipelines/1604082676059/component/a0db-a8d9/configuration\"},{\"@graph\":[{\"@id\":\"http://localhost:8080/resources/pipelines/1604082676059/component/b3a5-8b12/configuration\",\"@type\":[\"http://plugins.linkedpipes.com/ontology/t-packZip#Configuration\"],\"http://plugins.linkedpipes.com/ontology/t-packZip#fileName\":[{\"@value\":\"output.ttl\"}],\"http://plugins.linkedpipes.com/ontology/t-packZip#fileNameControl\":[{\"@id\":\"http://plugins.linkedpipes.com/resource/configuration/None\"}]}],\"@id\":\"http://localhost:8080/resources/pipelines/1604082676059/component/b3a5-8b12/configuration\"}]"

        const val EVERYTHING = "[\n" +
                "  {\n" +
                "    \"@graph\": [\n" +
                "      {\n" +
                "        \"@id\": \"http://localhost:8080/resources/components/1605898591497-d0599471-bd73-47a1-9a52-e546546291f4\",\n" +
                "        \"@type\": [\n" +
                "          \"http://linkedpipes.com/ontology/Template\"\n" +
                "        ],\n" +
                "        \"http://linkedpipes.com/ontology/configurationGraph\": [\n" +
                "          {\n" +
                "            \"@id\": \"http://localhost:8080/resources/components/1605898591497-d0599471-bd73-47a1-9a52-e546546291f4/configuration\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://linkedpipes.com/ontology/template\": [\n" +
                "          {\n" +
                "            \"@id\": \"http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://www.w3.org/2004/02/skos/core#prefLabel\": [\n" +
                "          {\n" +
                "            \"@value\": \"my_http_template\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ],\n" +
                "    \"@id\": \"http://localhost:8080/resources/components/1605898591497-d0599471-bd73-47a1-9a52-e546546291f4\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"@graph\": [\n" +
                "      {\n" +
                "        \"@id\": \"http://localhost:8080/resources/components/1605898591497-d0599471-bd73-47a1-9a52-e546546291f4/configuration/0\",\n" +
                "        \"@type\": [\n" +
                "          \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#Configuration\"\n" +
                "        ],\n" +
                "        \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#encodeUrl\": [\n" +
                "          {\n" +
                "            \"@type\": \"http://www.w3.org/2001/XMLSchema#boolean\",\n" +
                "            \"@value\": \"false\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#encodeUrlControl\": [\n" +
                "          {\n" +
                "            \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileName\": [\n" +
                "          {\n" +
                "            \"@value\": \"file.txt\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileNameControl\": [\n" +
                "          {\n" +
                "            \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileUri\": [\n" +
                "          {\n" +
                "            \"@value\": \"http://localhost:1234/file.txt\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileUriControl\": [\n" +
                "          {\n" +
                "            \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#hardRedirect\": [\n" +
                "          {\n" +
                "            \"@type\": \"http://www.w3.org/2001/XMLSchema#boolean\",\n" +
                "            \"@value\": \"true\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#hardRedirectControl\": [\n" +
                "          {\n" +
                "            \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#userAgent\": [\n" +
                "          {\n" +
                "            \"@value\": \"\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#userAgentControl\": [\n" +
                "          {\n" +
                "            \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#utf8Redirect\": [\n" +
                "          {\n" +
                "            \"@type\": \"http://www.w3.org/2001/XMLSchema#boolean\",\n" +
                "            \"@value\": \"false\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#utf8RedirectControl\": [\n" +
                "          {\n" +
                "            \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ],\n" +
                "    \"@id\": \"http://localhost:8080/resources/components/1605898591497-d0599471-bd73-47a1-9a52-e546546291f4/configuration\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"@graph\": [\n" +
                "      {\n" +
                "        \"@id\": \"http://localhost:8080/resources/components/1605912157929-047bb7a0-0f71-4d0a-abab-3da5793060e0\",\n" +
                "        \"@type\": [\n" +
                "          \"http://linkedpipes.com/ontology/Template\"\n" +
                "        ],\n" +
                "        \"http://linkedpipes.com/ontology/configurationGraph\": [\n" +
                "          {\n" +
                "            \"@id\": \"http://localhost:8080/resources/components/1605912157929-047bb7a0-0f71-4d0a-abab-3da5793060e0/configuration\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://linkedpipes.com/ontology/template\": [\n" +
                "          {\n" +
                "            \"@id\": \"http://etl.linkedpipes.com/resources/components/l-filesToLocal/0.0.0\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://www.w3.org/2004/02/skos/core#prefLabel\": [\n" +
                "          {\n" +
                "            \"@value\": \"my_files_to_local_template\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ],\n" +
                "    \"@id\": \"http://localhost:8080/resources/components/1605912157929-047bb7a0-0f71-4d0a-abab-3da5793060e0\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"@graph\": [\n" +
                "      {\n" +
                "        \"@id\": \"http://localhost:8080/resources/components/1605912157929-047bb7a0-0f71-4d0a-abab-3da5793060e0/configuration/0\",\n" +
                "        \"@type\": [\n" +
                "          \"http://plugins.linkedpipes.com/ontology/l-filesToLocal#Configuration\"\n" +
                "        ],\n" +
                "        \"http://plugins.linkedpipes.com/ontology/l-filesToLocal#path\": [\n" +
                "          {\n" +
                "            \"@value\": \"my_target_path\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://plugins.linkedpipes.com/ontology/l-filesToLocal#pathControl\": [\n" +
                "          {\n" +
                "            \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ],\n" +
                "    \"@id\": \"http://localhost:8080/resources/components/1605912157929-047bb7a0-0f71-4d0a-abab-3da5793060e0/configuration\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"@graph\": [\n" +
                "      {\n" +
                "        \"@id\": \"http://localhost:8080/resources/pipelines/1605898391174\",\n" +
                "        \"@type\": [\n" +
                "          \"http://linkedpipes.com/ontology/Pipeline\"\n" +
                "        ],\n" +
                "        \"http://etl.linkedpipes.com/ontology/version\": [\n" +
                "          {\n" +
                "            \"@type\": \"http://www.w3.org/2001/XMLSchema#int\",\n" +
                "            \"@value\": \"2\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://linkedpipes.com/ontology/profile\": [\n" +
                "          {\n" +
                "            \"@id\": \"http://localhost:8080/resources/pipelines/1605898391174/profile/default\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://www.w3.org/2004/02/skos/core#prefLabel\": [\n" +
                "          {\n" +
                "            \"@value\": \"everything\"\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      {\n" +
                "        \"@id\": \"http://localhost:8080/resources/pipelines/1605898391174/component/370b-873a\",\n" +
                "        \"@type\": [\n" +
                "          \"http://linkedpipes.com/ontology/Component\"\n" +
                "        ],\n" +
                "        \"http://linkedpipes.com/ontology/configurationGraph\": [\n" +
                "          {\n" +
                "            \"@id\": \"http://localhost:8080/resources/pipelines/1605898391174/component/370b-873a/configuration\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://linkedpipes.com/ontology/template\": [\n" +
                "          {\n" +
                "            \"@id\": \"http://localhost:8080/resources/components/1605912157929-047bb7a0-0f71-4d0a-abab-3da5793060e0\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://linkedpipes.com/ontology/x\": [\n" +
                "          {\n" +
                "            \"@type\": \"http://www.w3.org/2001/XMLSchema#integer\",\n" +
                "            \"@value\": \"63\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://linkedpipes.com/ontology/y\": [\n" +
                "          {\n" +
                "            \"@type\": \"http://www.w3.org/2001/XMLSchema#integer\",\n" +
                "            \"@value\": \"185\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://www.w3.org/2004/02/skos/core#prefLabel\": [\n" +
                "          {\n" +
                "            \"@value\": \"Files to local\"\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      {\n" +
                "        \"@id\": \"http://localhost:8080/resources/pipelines/1605898391174/component/a653-b5dc\",\n" +
                "        \"@type\": [\n" +
                "          \"http://linkedpipes.com/ontology/Component\"\n" +
                "        ],\n" +
                "        \"http://linkedpipes.com/ontology/configurationGraph\": [\n" +
                "          {\n" +
                "            \"@id\": \"http://localhost:8080/resources/pipelines/1605898391174/component/a653-b5dc/configuration\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://linkedpipes.com/ontology/template\": [\n" +
                "          {\n" +
                "            \"@id\": \"http://localhost:8080/resources/components/1605898591497-d0599471-bd73-47a1-9a52-e546546291f4\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://linkedpipes.com/ontology/x\": [\n" +
                "          {\n" +
                "            \"@type\": \"http://www.w3.org/2001/XMLSchema#integer\",\n" +
                "            \"@value\": \"79\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://linkedpipes.com/ontology/y\": [\n" +
                "          {\n" +
                "            \"@type\": \"http://www.w3.org/2001/XMLSchema#integer\",\n" +
                "            \"@value\": \"72\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://www.w3.org/2004/02/skos/core#prefLabel\": [\n" +
                "          {\n" +
                "            \"@value\": \"HTTP get\"\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      {\n" +
                "        \"@id\": \"http://localhost:8080/resources/pipelines/1605898391174/connection/d1b2-a76d\",\n" +
                "        \"@type\": [\n" +
                "          \"http://linkedpipes.com/ontology/Connection\"\n" +
                "        ],\n" +
                "        \"http://linkedpipes.com/ontology/sourceBinding\": [\n" +
                "          {\n" +
                "            \"@value\": \"FilesOutput\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://linkedpipes.com/ontology/sourceComponent\": [\n" +
                "          {\n" +
                "            \"@id\": \"http://localhost:8080/resources/pipelines/1605898391174/component/a653-b5dc\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://linkedpipes.com/ontology/targetBinding\": [\n" +
                "          {\n" +
                "            \"@value\": \"FilesInput\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://linkedpipes.com/ontology/targetComponent\": [\n" +
                "          {\n" +
                "            \"@id\": \"http://localhost:8080/resources/pipelines/1605898391174/component/370b-873a\"\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      {\n" +
                "        \"@id\": \"http://localhost:8080/resources/pipelines/1605898391174/profile/default\",\n" +
                "        \"@type\": [\n" +
                "          \"http://linkedpipes.com/ontology/ExecutionProfile\"\n" +
                "        ],\n" +
                "        \"http://linkedpipes.com/ontology/rdfRepositoryPolicy\": [\n" +
                "          {\n" +
                "            \"@id\": \"http://linkedpipes.com/ontology/repository/SingleRepository\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://linkedpipes.com/ontology/rdfRepositoryType\": [\n" +
                "          {\n" +
                "            \"@id\": \"http://linkedpipes.com/ontology/repository/NativeStore\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ],\n" +
                "    \"@id\": \"http://localhost:8080/resources/pipelines/1605898391174\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"@graph\": [\n" +
                "      {\n" +
                "        \"@id\": \"http://localhost:8080/resources/pipelines/1605898391174/component/370b-873a/configuration\",\n" +
                "        \"@type\": [\n" +
                "          \"http://plugins.linkedpipes.com/ontology/l-filesToLocal#Configuration\"\n" +
                "        ],\n" +
                "        \"http://plugins.linkedpipes.com/ontology/l-filesToLocal#path\": [\n" +
                "          {\n" +
                "            \"@value\": \"my_target_path\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://plugins.linkedpipes.com/ontology/l-filesToLocal#pathControl\": [\n" +
                "          {\n" +
                "            \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ],\n" +
                "    \"@id\": \"http://localhost:8080/resources/pipelines/1605898391174/component/370b-873a/configuration\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"@graph\": [\n" +
                "      {\n" +
                "        \"@id\": \"http://localhost:8080/resources/pipelines/1605898391174/component/a653-b5dc/configuration\",\n" +
                "        \"@type\": [\n" +
                "          \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#Configuration\"\n" +
                "        ],\n" +
                "        \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#encodeUrl\": [\n" +
                "          {\n" +
                "            \"@type\": \"http://www.w3.org/2001/XMLSchema#boolean\",\n" +
                "            \"@value\": \"false\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#encodeUrlControl\": [\n" +
                "          {\n" +
                "            \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileName\": [\n" +
                "          {\n" +
                "            \"@value\": \"file.txt\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileNameControl\": [\n" +
                "          {\n" +
                "            \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileUri\": [\n" +
                "          {\n" +
                "            \"@value\": \"http://localhost:1234/file.txt\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileUriControl\": [\n" +
                "          {\n" +
                "            \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#hardRedirect\": [\n" +
                "          {\n" +
                "            \"@type\": \"http://www.w3.org/2001/XMLSchema#boolean\",\n" +
                "            \"@value\": \"true\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#hardRedirectControl\": [\n" +
                "          {\n" +
                "            \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#userAgent\": [\n" +
                "          {\n" +
                "            \"@value\": \"\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#userAgentControl\": [\n" +
                "          {\n" +
                "            \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#utf8Redirect\": [\n" +
                "          {\n" +
                "            \"@type\": \"http://www.w3.org/2001/XMLSchema#boolean\",\n" +
                "            \"@value\": \"false\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#utf8RedirectControl\": [\n" +
                "          {\n" +
                "            \"@id\": \"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ],\n" +
                "    \"@id\": \"http://localhost:8080/resources/pipelines/1605898391174/component/a653-b5dc/configuration\"\n" +
                "  }\n" +
                "]"
    }
}
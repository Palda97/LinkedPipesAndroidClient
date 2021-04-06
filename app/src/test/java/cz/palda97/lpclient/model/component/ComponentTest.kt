package cz.palda97.lpclient.model.component

import cz.palda97.lpclient.*
import cz.palda97.lpclient.model.entities.possiblecomponent.PossibleComponentFactory
import org.junit.Test

import org.junit.Assert.*

class ComponentTest
    : PowerMockTest() {

    @Test
    fun parseTest() {
        val serverId = 777L
        val json = LIGHT_LIST
        val factory = PossibleComponentFactory(json, serverId)
        val list = factory.parse()
        assertNotNull("parsing failed", list)
        list!!
        assertEquals(2, list.size)
        val labels = listOf(
            "download crab",
            "HTTP get"
        )
        assertListContentMatch("labels not match", labels, list.map { it.prefLabel })
        assertTrue(list.all { it.serverId == serverId })
    }

    @Test
    fun largeTest() {
        val json = stringFromFile(COMPONENT_LIST)
        val factory = PossibleComponentFactory(json, 777L)
        val list = factory.parse()
        assertNotNull("parsing failed", list)
        list!!
        assertEquals(92, list.size)
        val templateCnt = list.count {
            it.templateId != null
        }
        assertEquals(3, templateCnt)
        val distinct = list.distinctBy {
            it.id
        }
        assertEquals(list.size, distinct.size)
    }

    companion object {
        private const val COMPONENT_LIST = "component_list.jsonld"
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
    }
}
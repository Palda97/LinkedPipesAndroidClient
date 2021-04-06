package cz.palda97.lpclient.model.pipeline

import cz.palda97.lpclient.*
import cz.palda97.lpclient.model.entities.pipeline.Binding
import cz.palda97.lpclient.model.entities.pipeline.BindingFactory
import org.junit.Test

import org.junit.Assert.*

class BindingFactoryTest
    : PowerMockTest() {

    @Test
    fun bindingSide() {
        val input = Binding("", Binding.Type.INPUT, "", "", "")
        val output = Binding("", Binding.Type.OUTPUT, "", "", "")
        val config = Binding("", Binding.Type.CONFIGURATION, "", "", "")
        assertFalse(input isSameSideAs output)
        assertTrue(input isSameSideAs config)
        assertFalse(output isSameSideAs config)
    }

    @Test
    fun parseFileHasher() {
        val json = FILE_HASHER
        val bindings = BindingFactory(json).parse()
        assertNotNull("binding factory returned null", bindings)
        bindings!!
        val expectedBindings = listOf(
            Binding(
                "http://etl.linkedpipes.com/resources/components/t-fileHasher/0.0.0",
                Binding.Type.INPUT,
                "InputFiles",
                "Input",
                "http://etl.linkedpipes.com/resources/components/t-fileHasher/0.0.0/input"
            ),
            Binding(
                "http://etl.linkedpipes.com/resources/components/t-fileHasher/0.0.0",
                Binding.Type.OUTPUT,
                "OutputRdf",
                "Output",
                "http://etl.linkedpipes.com/resources/components/t-fileHasher/0.0.0/output"
            )
        )
        assertListContentMatch(expectedBindings, bindings)

        //For broken coverage
        val binding = bindings.find { it.bindingValue == "InputFiles" }!!
        assertEquals("http://etl.linkedpipes.com/resources/components/t-fileHasher/0.0.0", binding.templateId)
        assertEquals(Binding.Type.INPUT, binding.type)
        assertEquals("Input", binding.prefLabel)
        assertEquals("http://etl.linkedpipes.com/resources/components/t-fileHasher/0.0.0/input", binding.id)
    }

    @Test
    fun parseHttpGet() {
        val json = HTTP_GET
        val bindings = BindingFactory(json).parse()
        assertNotNull("binding factory returned null", bindings)
        bindings!!
        val expectedBindings = listOf(
            Binding(
                "http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0",
                Binding.Type.CONFIGURATION,
                "Configuration",
                "Configuration",
                "http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0/configuration"
            ),
            Binding(
                "http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0",
                Binding.Type.OUTPUT,
                "FilesOutput",
                "Output",
                "http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0/output"
            )
        )
        assertListContentMatch(expectedBindings, bindings)
    }

    @Test
    fun parseZip() {
        val json = ZIP
        val bindings = BindingFactory(json).parse()
        assertNotNull("binding factory returned null", bindings)
        bindings!!
        val expectedBindings = listOf(
            Binding(
                "http://etl.linkedpipes.com/resources/components/t-packZip/0.0.0",
                Binding.Type.CONFIGURATION,
                "Configuration",
                "Configuration",
                "http://etl.linkedpipes.com/resources/components/t-packZip/0.0.0/configuration"
            ),
            Binding(
                "http://etl.linkedpipes.com/resources/components/t-packZip/0.0.0",
                Binding.Type.INPUT,
                "FilesInput",
                "Output",
                "http://etl.linkedpipes.com/resources/components/t-packZip/0.0.0/input"
            ),
            Binding(
                "http://etl.linkedpipes.com/resources/components/t-packZip/0.0.0",
                Binding.Type.OUTPUT,
                "FilesOutput",
                "Output",
                "http://etl.linkedpipes.com/resources/components/t-packZip/0.0.0/output"
            )
        )
        assertListContentMatch(expectedBindings, bindings)
    }

    companion object {

        private const val FILE_HASHER = "[\n" +
                "   {\n" +
                "      \"@graph\":[\n" +
                "         {\n" +
                "            \"@id\":\"http://etl.linkedpipes.com/resources/components/t-fileHasher/0.0.0\",\n" +
                "            \"@type\":[\n" +
                "               \"http://linkedpipes.com/ontology/JarTemplate\"\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/color\":[\n" +
                "               {\n" +
                "                  \"@value\":\"#CED8F6\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/componentType\":[\n" +
                "               {\n" +
                "                  \"@id\":\"http://etl.linkedpipes.com/ontology/component/type/Transformer\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/configurationDescription\":[\n" +
                "               {\n" +
                "                  \"@id\":\"http://linkedpipes.com/resources/components/t-fileHasher/0.0.0/configuration/desc\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/configurationGraph\":[\n" +
                "               {\n" +
                "                  \"@id\":\"http://etl.linkedpipes.com/resources/components/t-fileHasher/0.0.0/configGraph\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/infoLink\":[\n" +
                "               {\n" +
                "                  \"@id\":\"https://etl.linkedpipes.com/components/t-filehasher\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/jar\":[\n" +
                "               {\n" +
                "                  \"@id\":\"http://etl.linkedpipes.com/resources/jars/t-fileHasher/0.0.0\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/keyword\":[\n" +
                "               {\n" +
                "                  \"@value\":\"hash\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"@value\":\"sha1\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"@value\":\"check\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/port\":[\n" +
                "               {\n" +
                "                  \"@id\":\"http://etl.linkedpipes.com/resources/components/t-fileHasher/0.0.0/input\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"@id\":\"http://etl.linkedpipes.com/resources/components/t-fileHasher/0.0.0/output\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/requirement\":[\n" +
                "               {\n" +
                "                  \"@id\":\"http://linkedpipes.com/resources/requirement/workingDirectory\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/supportControl\":[\n" +
                "               {\n" +
                "                  \"@type\":\"http://www.w3.org/2001/XMLSchema#boolean\",\n" +
                "                  \"@value\":\"false\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://www.w3.org/2004/02/skos/core#prefLabel\":[\n" +
                "               {\n" +
                "                  \"@value\":\"File hasher\"\n" +
                "               }\n" +
                "            ]\n" +
                "         },\n" +
                "         {\n" +
                "            \"@id\":\"http://etl.linkedpipes.com/resources/components/t-fileHasher/0.0.0/input\",\n" +
                "            \"@type\":[\n" +
                "               \"http://linkedpipes.com/ontology/dataUnit/system/1.0/files/DirectoryMirror\",\n" +
                "               \"http://linkedpipes.com/ontology/Port\",\n" +
                "               \"http://linkedpipes.com/ontology/Input\"\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/binding\":[\n" +
                "               {\n" +
                "                  \"@value\":\"InputFiles\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/requirement\":[\n" +
                "               {\n" +
                "                  \"@id\":\"http://linkedpipes.com/resources/requirement/workingDirectory\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://www.w3.org/2004/02/skos/core#prefLabel\":[\n" +
                "               {\n" +
                "                  \"@value\":\"Input\"\n" +
                "               }\n" +
                "            ]\n" +
                "         },\n" +
                "         {\n" +
                "            \"@id\":\"http://etl.linkedpipes.com/resources/components/t-fileHasher/0.0.0/output\",\n" +
                "            \"@type\":[\n" +
                "               \"http://linkedpipes.com/ontology/dataUnit/sesame/1.0/rdf/SingleGraph\",\n" +
                "               \"http://linkedpipes.com/ontology/Port\",\n" +
                "               \"http://linkedpipes.com/ontology/Output\"\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/binding\":[\n" +
                "               {\n" +
                "                  \"@value\":\"OutputRdf\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://www.w3.org/2004/02/skos/core#prefLabel\":[\n" +
                "               {\n" +
                "                  \"@value\":\"Output\"\n" +
                "               }\n" +
                "            ]\n" +
                "         }\n" +
                "      ],\n" +
                "      \"@id\":\"http://etl.linkedpipes.com/resources/components/t-fileHasher/0.0.0\"\n" +
                "   }\n" +
                "]"

        private const val HTTP_GET = "[\n" +
                "   {\n" +
                "      \"@graph\":[\n" +
                "         {\n" +
                "            \"@id\":\"http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0\",\n" +
                "            \"@type\":[\n" +
                "               \"http://linkedpipes.com/ontology/JarTemplate\"\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/color\":[\n" +
                "               {\n" +
                "                  \"@value\":\"#F6D8CE\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/componentType\":[\n" +
                "               {\n" +
                "                  \"@id\":\"http://etl.linkedpipes.com/ontology/component/type/Extractor\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/configurationDescription\":[\n" +
                "               {\n" +
                "                  \"@id\":\"http://linkedpipes.com/resources/components/e-httpGetFile/0.0.0/configuration/desc\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/configurationGraph\":[\n" +
                "               {\n" +
                "                  \"@id\":\"http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0/configGraph\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/dialog\":[\n" +
                "               {\n" +
                "                  \"@id\":\"http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0/dialog/template\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"@id\":\"http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0/dialog/instance\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"@id\":\"http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0/dialog/config\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/infoLink\":[\n" +
                "               {\n" +
                "                  \"@id\":\"https://etl.linkedpipes.com/components/e-httpgetfile\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/jar\":[\n" +
                "               {\n" +
                "                  \"@id\":\"http://etl.linkedpipes.com/resources/jars/e-httpGetFile/0.0.0\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/keyword\":[\n" +
                "               {\n" +
                "                  \"@value\":\"HTTP\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"@value\":\"get\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/port\":[\n" +
                "               {\n" +
                "                  \"@id\":\"http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0/output\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"@id\":\"http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0/configuration\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/requirement\":[\n" +
                "               {\n" +
                "                  \"@id\":\"http://linkedpipes.com/resources/requirement/workingDirectory\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/supportControl\":[\n" +
                "               {\n" +
                "                  \"@type\":\"http://www.w3.org/2001/XMLSchema#boolean\",\n" +
                "                  \"@value\":\"true\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://www.w3.org/2004/02/skos/core#prefLabel\":[\n" +
                "               {\n" +
                "                  \"@value\":\"HTTP get\"\n" +
                "               }\n" +
                "            ]\n" +
                "         },\n" +
                "         {\n" +
                "            \"@id\":\"http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0/configuration\",\n" +
                "            \"@type\":[\n" +
                "               \"http://linkedpipes.com/ontology/dataUnit/sesame/1.0/rdf/SingleGraph\",\n" +
                "               \"http://linkedpipes.com/ontology/RuntimeConfiguration\"\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/binding\":[\n" +
                "               {\n" +
                "                  \"@value\":\"Configuration\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://www.w3.org/2004/02/skos/core#prefLabel\":[\n" +
                "               {\n" +
                "                  \"@value\":\"Configuration\"\n" +
                "               }\n" +
                "            ]\n" +
                "         },\n" +
                "         {\n" +
                "            \"@id\":\"http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0/dialog/config\",\n" +
                "            \"@type\":[\n" +
                "               \"http://linkedpipes.com/ontology/Dialog\"\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/name\":[\n" +
                "               {\n" +
                "                  \"@value\":\"config\"\n" +
                "               }\n" +
                "            ]\n" +
                "         },\n" +
                "         {\n" +
                "            \"@id\":\"http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0/dialog/instance\",\n" +
                "            \"@type\":[\n" +
                "               \"http://linkedpipes.com/ontology/Dialog\"\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/name\":[\n" +
                "               {\n" +
                "                  \"@value\":\"instance\"\n" +
                "               }\n" +
                "            ]\n" +
                "         },\n" +
                "         {\n" +
                "            \"@id\":\"http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0/dialog/template\",\n" +
                "            \"@type\":[\n" +
                "               \"http://linkedpipes.com/ontology/Dialog\"\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/name\":[\n" +
                "               {\n" +
                "                  \"@value\":\"template\"\n" +
                "               }\n" +
                "            ]\n" +
                "         },\n" +
                "         {\n" +
                "            \"@id\":\"http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0/output\",\n" +
                "            \"@type\":[\n" +
                "               \"http://linkedpipes.com/ontology/dataUnit/system/1.0/files/DirectoryMirror\",\n" +
                "               \"http://linkedpipes.com/ontology/Output\"\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/binding\":[\n" +
                "               {\n" +
                "                  \"@value\":\"FilesOutput\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/requirement\":[\n" +
                "               {\n" +
                "                  \"@id\":\"http://linkedpipes.com/resources/requirement/workingDirectory\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://www.w3.org/2004/02/skos/core#prefLabel\":[\n" +
                "               {\n" +
                "                  \"@value\":\"Output\"\n" +
                "               }\n" +
                "            ]\n" +
                "         }\n" +
                "      ],\n" +
                "      \"@id\":\"http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0\"\n" +
                "   }\n" +
                "]"

        private const val ZIP = "[\n" +
                "   {\n" +
                "      \"@graph\":[\n" +
                "         {\n" +
                "            \"@id\":\"http://etl.linkedpipes.com/resources/components/t-packZip/0.0.0\",\n" +
                "            \"@type\":[\n" +
                "               \"http://linkedpipes.com/ontology/JarTemplate\"\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/color\":[\n" +
                "               {\n" +
                "                  \"@value\":\"#CED8F6\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/componentType\":[\n" +
                "               {\n" +
                "                  \"@id\":\"http://etl.linkedpipes.com/ontology/component/type/Transformer\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/configurationDescription\":[\n" +
                "               {\n" +
                "                  \"@id\":\"http://linkedpipes.com/resources/components/t-packZip/0.0.0/configuration/desc\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/configurationGraph\":[\n" +
                "               {\n" +
                "                  \"@id\":\"http://etl.linkedpipes.com/resources/components/t-packZip/0.0.0/configGraph\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/dialog\":[\n" +
                "               {\n" +
                "                  \"@id\":\"http://etl.linkedpipes.com/resources/components/t-packZip/0.0.0/dialog/template\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"@id\":\"http://etl.linkedpipes.com/resources/components/t-packZip/0.0.0/dialog/instance\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"@id\":\"http://etl.linkedpipes.com/resources/components/t-packZip/0.0.0/dialog/config\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/infoLink\":[\n" +
                "               {\n" +
                "                  \"@id\":\"https://etl.linkedpipes.com/components/t-packzip\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/jar\":[\n" +
                "               {\n" +
                "                  \"@id\":\"http://etl.linkedpipes.com/resources/jars/t-packZip/0.0.0\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/keyword\":[\n" +
                "               {\n" +
                "                  \"@value\":\"zip\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"@value\":\"compress\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/port\":[\n" +
                "               {\n" +
                "                  \"@id\":\"http://etl.linkedpipes.com/resources/components/t-packZip/0.0.0/output\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"@id\":\"http://etl.linkedpipes.com/resources/components/t-packZip/0.0.0/input\"\n" +
                "               },\n" +
                "               {\n" +
                "                  \"@id\":\"http://etl.linkedpipes.com/resources/components/t-packZip/0.0.0/configuration\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/requirement\":[\n" +
                "               {\n" +
                "                  \"@id\":\"http://linkedpipes.com/resources/requirement/workingDirectory\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/supportControl\":[\n" +
                "               {\n" +
                "                  \"@type\":\"http://www.w3.org/2001/XMLSchema#boolean\",\n" +
                "                  \"@value\":\"true\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://www.w3.org/2004/02/skos/core#prefLabel\":[\n" +
                "               {\n" +
                "                  \"@value\":\"Create zip archive\"\n" +
                "               }\n" +
                "            ]\n" +
                "         },\n" +
                "         {\n" +
                "            \"@id\":\"http://etl.linkedpipes.com/resources/components/t-packZip/0.0.0/configuration\",\n" +
                "            \"@type\":[\n" +
                "               \"http://linkedpipes.com/ontology/dataUnit/sesame/1.0/rdf/SingleGraph\",\n" +
                "               \"http://linkedpipes.com/ontology/RuntimeConfiguration\"\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/binding\":[\n" +
                "               {\n" +
                "                  \"@value\":\"Configuration\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://www.w3.org/2004/02/skos/core#prefLabel\":[\n" +
                "               {\n" +
                "                  \"@value\":\"Configuration\"\n" +
                "               }\n" +
                "            ]\n" +
                "         },\n" +
                "         {\n" +
                "            \"@id\":\"http://etl.linkedpipes.com/resources/components/t-packZip/0.0.0/dialog/config\",\n" +
                "            \"@type\":[\n" +
                "               \"http://linkedpipes.com/ontology/Dialog\"\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/name\":[\n" +
                "               {\n" +
                "                  \"@value\":\"config\"\n" +
                "               }\n" +
                "            ]\n" +
                "         },\n" +
                "         {\n" +
                "            \"@id\":\"http://etl.linkedpipes.com/resources/components/t-packZip/0.0.0/dialog/instance\",\n" +
                "            \"@type\":[\n" +
                "               \"http://linkedpipes.com/ontology/Dialog\"\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/name\":[\n" +
                "               {\n" +
                "                  \"@value\":\"instance\"\n" +
                "               }\n" +
                "            ]\n" +
                "         },\n" +
                "         {\n" +
                "            \"@id\":\"http://etl.linkedpipes.com/resources/components/t-packZip/0.0.0/dialog/template\",\n" +
                "            \"@type\":[\n" +
                "               \"http://linkedpipes.com/ontology/Dialog\"\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/name\":[\n" +
                "               {\n" +
                "                  \"@value\":\"template\"\n" +
                "               }\n" +
                "            ]\n" +
                "         },\n" +
                "         {\n" +
                "            \"@id\":\"http://etl.linkedpipes.com/resources/components/t-packZip/0.0.0/input\",\n" +
                "            \"@type\":[\n" +
                "               \"http://linkedpipes.com/ontology/dataUnit/system/1.0/files/DirectoryMirror\",\n" +
                "               \"http://linkedpipes.com/ontology/Port\",\n" +
                "               \"http://linkedpipes.com/ontology/Input\"\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/binding\":[\n" +
                "               {\n" +
                "                  \"@value\":\"FilesInput\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/requirement\":[\n" +
                "               {\n" +
                "                  \"@id\":\"http://linkedpipes.com/resources/requirement/workingDirectory\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://www.w3.org/2004/02/skos/core#prefLabel\":[\n" +
                "               {\n" +
                "                  \"@value\":\"Output\"\n" +
                "               }\n" +
                "            ]\n" +
                "         },\n" +
                "         {\n" +
                "            \"@id\":\"http://etl.linkedpipes.com/resources/components/t-packZip/0.0.0/output\",\n" +
                "            \"@type\":[\n" +
                "               \"http://linkedpipes.com/ontology/dataUnit/system/1.0/files/DirectoryMirror\",\n" +
                "               \"http://linkedpipes.com/ontology/Port\",\n" +
                "               \"http://linkedpipes.com/ontology/Output\"\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/binding\":[\n" +
                "               {\n" +
                "                  \"@value\":\"FilesOutput\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://linkedpipes.com/ontology/requirement\":[\n" +
                "               {\n" +
                "                  \"@id\":\"http://linkedpipes.com/resources/requirement/workingDirectory\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://www.w3.org/2004/02/skos/core#prefLabel\":[\n" +
                "               {\n" +
                "                  \"@value\":\"Output\"\n" +
                "               }\n" +
                "            ]\n" +
                "         }\n" +
                "      ],\n" +
                "      \"@id\":\"http://etl.linkedpipes.com/resources/components/t-packZip/0.0.0\"\n" +
                "   }\n" +
                "]"
    }
}
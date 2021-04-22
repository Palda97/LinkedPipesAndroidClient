package cz.palda97.lpclient.model.pipeline

import cz.palda97.lpclient.*
import cz.palda97.lpclient.model.entities.pipeline.DialogJsFactory
import cz.palda97.lpclient.model.entities.pipeline.PipelineFactory
import cz.palda97.lpclient.model.travelobjects.LdConstants
import org.junit.Test

import org.junit.Assert.*

class ControlCrossTest
    : MockkTest() {

    @Test
    fun test() {
        val dialogJs = DialogJsFactory(DialogJsFactoryTest.HTTP_GET, DialogJsFactoryTest.COMPONENT_ID).parse()!!
        val configuration = PipelineFactory(PipelineParsingTest.server, EMPTY_HTTP_GET_CONFIGURATION).parseConfigurationOnly().mailContent!!
        val controlList = configuration.getInheritances(dialogJs.controlRegex, dialogJs.configType)

        assertNotNull("control list is null", controlList)
        controlList!!

        val expectedList = listOf(
            "http://plugins.linkedpipes.com/ontology/e-httpGetFile#encodeUrlControl" to false,
            "http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileNameControl" to false,
            "http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileUriControl" to false,
            "http://plugins.linkedpipes.com/ontology/e-httpGetFile#hardRedirectControl" to false,
            "http://plugins.linkedpipes.com/ontology/e-httpGetFile#userAgentControl" to false,
            "http://plugins.linkedpipes.com/ontology/e-httpGetFile#utf8RedirectControl" to false
        )

        assertListContentMatch(expectedList, controlList)

        val changeKey = "http://plugins.linkedpipes.com/ontology/e-httpGetFile#utf8RedirectControl"
        configuration.setInheritance(changeKey, LdConstants.INHERITANCE_INHERIT, dialogJs.configType)
        val inheritances = configuration.getInheritances(dialogJs.controlRegex, dialogJs.configType)
        assertNotNull(inheritances)
        inheritances!!
        assertEquals(true, inheritances.find { it.first == changeKey }?.second)
    }

    companion object {

        private const val EMPTY_HTTP_GET_CONFIGURATION = "[{\n" +
                "      \"@graph\":[\n" +
                "         {\n" +
                "            \"@id\":\"http://etl.linkedpipes.com/resources/components/e-httpGetFile/0.0.0/new/1\",\n" +
                "            \"@type\":[\n" +
                "               \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#Configuration\"\n" +
                "            ],\n" +
                "            \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#encodeUrlControl\":[\n" +
                "               {\n" +
                "                  \"@id\":\"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileName\":[\n" +
                "               {\n" +
                "                  \"@value\":\"\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileNameControl\":[\n" +
                "               {\n" +
                "                  \"@id\":\"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileUri\":[\n" +
                "               {\n" +
                "                  \"@value\":\"\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileUriControl\":[\n" +
                "               {\n" +
                "                  \"@id\":\"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#hardRedirect\":[\n" +
                "               {\n" +
                "                  \"@type\":\"http://www.w3.org/2001/XMLSchema#boolean\",\n" +
                "                  \"@value\":\"false\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#hardRedirectControl\":[\n" +
                "               {\n" +
                "                  \"@id\":\"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#userAgent\":[\n" +
                "               {\n" +
                "                  \"@value\":\"\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#userAgentControl\":[\n" +
                "               {\n" +
                "                  \"@id\":\"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "               }\n" +
                "            ],\n" +
                "            \"http://plugins.linkedpipes.com/ontology/e-httpGetFile#utf8RedirectControl\":[\n" +
                "               {\n" +
                "                  \"@id\":\"http://plugins.linkedpipes.com/resource/configuration/None\"\n" +
                "               }\n" +
                "            ]\n" +
                "         }\n" +
                "      ],\n" +
                "      \"@id\":\"http://localhost:8080/resources/pipelines/1604082676059/component/a0db-a8d9/configuration\"\n" +
                "   }]"
    }
}
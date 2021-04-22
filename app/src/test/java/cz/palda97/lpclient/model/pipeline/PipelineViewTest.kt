package cz.palda97.lpclient.model.pipeline

import cz.palda97.lpclient.*
import cz.palda97.lpclient.model.entities.pipelineview.PipelineViewFactory
import org.junit.Test
import org.junit.Assert.*

class PipelineViewTest
    : MockkTest() {

    @Test
    fun parse() {
        val factory = PipelineViewFactory(SERVER, PIPELINE_VIEW_LIST)
        val content = factory.serverWithPipelineViews.mailContent
        assertNotNull(content)
        content!!
        assertEquals(SERVER, content.server)
        val pipelines = content.pipelineViewList.map { it.pipelineView }
        assertEquals(2, pipelines.size)
        //TODO(maybe test this in androidTest)
        val marks = content.pipelineViewList.map { it.mark }
        assertListContentMatch(listOf(null, null), marks)
    }

    companion object {

        private const val PIPELINE_VIEW_LIST = "[\n" +
                "    {\n" +
                "        \"@graph\": [\n" +
                "            {\n" +
                "                \"@id\": \"http://localhost:8080/resources/pipelines/1611789953288\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://linkedpipes.com/ontology/Pipeline\"\n" +
                "                ],\n" +
                "                \"http://www.w3.org/2004/02/skos/core#prefLabel\": [\n" +
                "                    {\n" +
                "                        \"@value\": \"LegendaryFoodOntology2\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ],\n" +
                "        \"@id\": \"http://localhost:8080/resources/pipelines/1611789953288\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"@graph\": [\n" +
                "            {\n" +
                "                \"@id\": \"http://localhost:8080/resources/pipelines/1611793092057\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://linkedpipes.com/ontology/Pipeline\"\n" +
                "                ],\n" +
                "                \"http://www.w3.org/2004/02/skos/core#prefLabel\": [\n" +
                "                    {\n" +
                "                        \"@value\": \"defCrab\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ],\n" +
                "        \"@id\": \"http://localhost:8080/resources/pipelines/1611793092057\"\n" +
                "    }\n" +
                "]"
    }
}
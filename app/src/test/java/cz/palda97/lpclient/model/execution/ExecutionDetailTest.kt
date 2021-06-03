package cz.palda97.lpclient.model.execution

import cz.palda97.lpclient.*
import cz.palda97.lpclient.model.entities.execution.*
import cz.palda97.lpclient.model.entities.pipeline.Component
import org.junit.Test
import org.junit.Assert.*

class ExecutionDetailTest
    : MockkTest() {

    @Test
    fun downloadAndStore() {
        val json = stringFromFile("executionDownloadAndStore.jsonld")
        val pipelineComponents = listOf(
            easyComponent("scp", "http://localhost:8080/resources/pipelines/1621514985033/component/4356-8f04"),
            easyComponent("download", "http://localhost:8080/resources/pipelines/1621514985033/component/a454-a506")
        )
        val executionId = "executionId"
        val factory = ExecutionDetailFactory(json, executionId, pipelineComponents)
        val components = factory.parse()
        assertNotNull("component list is null", components)
        components!!
        val expectedComponents = listOf(
            ExecutionDetailComponent(
                "http://localhost:8080/resources/pipelines/1621514985033/component/4356-8f04",
                executionId,
                ExecutionStatus.FAILED,
                2,
                "scp"
            ),
            ExecutionDetailComponent(
                "http://localhost:8080/resources/pipelines/1621514985033/component/a454-a506",
                executionId,
                ExecutionStatus.FINISHED,
                1,
                "download"
            )
        )
        assertListContentMatch(expectedComponents, components)
    }

    companion object {

        private fun easyComponent(label: String, id: String) = Component(null, "", 0, 0, label, null, id)
    }
}
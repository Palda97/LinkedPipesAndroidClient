package cz.palda97.lpclient.model.execution

import cz.palda97.lpclient.MockkTest
import cz.palda97.lpclient.model.entities.execution.ExecutionStatus
import cz.palda97.lpclient.model.entities.execution.ExecutionStatusUtilities
import org.junit.Test

import org.junit.Assert.*

class ExecutionStatusUtilitiesTest
    : MockkTest() {

    @Test
    fun fromJson() {
        val status = ExecutionStatusUtilities.fromDirectRequest(EXECUTION)
        assertNotNull(status)
        assertEquals(ExecutionStatus.FINISHED, status)
    }

    @Test
    fun statusFailed() {
        val status = ExecutionStatusUtilities.fromDirectRequest(EXECUTION_FAILED)
        assertNotNull("status is null", status)
        assertEquals(ExecutionStatus.FAILED, status)
    }

    companion object {

        private const val EXECUTION = "[ {\n" +
                "  \"@graph\" : [ {\n" +
                "    \"@id\" : \"http://localhost:8080/resources/executions/1601919679551-14-b6eb3a5b-306f-4cce-93b3-933ee423e96c\",\n" +
                "    \"@type\" : [ \"http://etl.linkedpipes.com/ontology/Execution\" ],\n" +
                "    \"http://etl.linkedpipes.com/ontology/pipeline\" : [ {\n" +
                "      \"@id\" : \"http://localhost:8080/resources/pipelines/1601051176625\"\n" +
                "    } ],\n" +
                "    \"http://etl.linkedpipes.com/ontology/status\" : [ {\n" +
                "      \"@id\" : \"http://etl.linkedpipes.com/resources/status/finished\"\n" +
                "    } ],\n" +
                "    \"http://linkedpipes.com/ontology/deleteWorkingData\" : [ {\n" +
                "      \"@type\" : \"http://www.w3.org/2001/XMLSchema#boolean\",\n" +
                "      \"@value\" : \"false\"\n" +
                "    } ]\n" +
                "  } ],\n" +
                "  \"@id\" : \"http://localhost:8080/resources/executions/1601919679551-14-b6eb3a5b-306f-4cce-93b3-933ee423e96c\"\n" +
                "} ]"

        private const val EXECUTION_FAILED = "[ {\n" +
                "      \"@graph\" : [ {\n" +
                "        \"@id\" : \"http://localhost:8080/resources/executions/1601940398843-7-9d4c5904-8df6-4ccb-93f0-ab66a2c0cafd\",\n" +
                "        \"@type\" : [ \"http://etl.linkedpipes.com/ontology/Execution\" ],\n" +
                "        \"http://etl.linkedpipes.com/ontology/pipeline\" : [ {\n" +
                "          \"@id\" : \"http://localhost:8080/resources/pipelines/1600968747469\"\n" +
                "        } ],\n" +
                "        \"http://etl.linkedpipes.com/ontology/status\" : [ {\n" +
                "          \"@id\" : \"http://etl.linkedpipes.com/resources/status/failed\"\n" +
                "        } ],\n" +
                "        \"http://linkedpipes.com/ontology/component\" : [ {\n" +
                "          \"@id\" : \"http://localhost:8080/resources/pipelines/1600968747469/component/9b63-878f\"\n" +
                "        } ],\n" +
                "        \"http://linkedpipes.com/ontology/deleteWorkingData\" : [ {\n" +
                "          \"@type\" : \"http://www.w3.org/2001/XMLSchema#boolean\",\n" +
                "          \"@value\" : \"false\"\n" +
                "        } ]\n" +
                "      }, {\n" +
                "        \"@id\" : \"http://localhost:8080/resources/pipelines/1600968747469/component/9b63-878f\",\n" +
                "        \"@type\" : [ \"http://linkedpipes.com/ontology/Component\" ],\n" +
                "        \"http://etl.linkedpipes.com/ontology/dataUnit\" : [ {\n" +
                "          \"@id\" : \"http://localhost:8080/resources/pipelines/1600968747469/component/9b63-878f/port/Configuration\"\n" +
                "        }, {\n" +
                "          \"@id\" : \"http://localhost:8080/resources/pipelines/1600968747469/component/9b63-878f/port/FilesOutput\"\n" +
                "        } ],\n" +
                "        \"http://etl.linkedpipes.com/ontology/status\" : [ {\n" +
                "          \"@id\" : \"http://etl.linkedpipes.com/resources/status/failed\"\n" +
                "        } ],\n" +
                "        \"http://linkedpipes.com/ontology/order\" : [ {\n" +
                "          \"@type\" : \"http://www.w3.org/2001/XMLSchema#int\",\n" +
                "          \"@value\" : \"1\"\n" +
                "        } ]\n" +
                "      }, {\n" +
                "        \"@id\" : \"http://localhost:8080/resources/pipelines/1600968747469/component/9b63-878f/port/Configuration\",\n" +
                "        \"@type\" : [ \"http://etl.linkedpipes.com/ontology/DataUnit\" ],\n" +
                "        \"http://etl.linkedpipes.com/ontology/binding\" : [ {\n" +
                "          \"@value\" : \"Configuration\"\n" +
                "        } ],\n" +
                "        \"http://etl.linkedpipes.com/ontology/dataPath\" : [ {\n" +
                "          \"@value\" : \"working/dataunit-000-2\"\n" +
                "        } ],\n" +
                "        \"http://etl.linkedpipes.com/ontology/debug\" : [ {\n" +
                "          \"@value\" : \"000\"\n" +
                "        } ]\n" +
                "      }, {\n" +
                "        \"@id\" : \"http://localhost:8080/resources/pipelines/1600968747469/component/9b63-878f/port/FilesOutput\",\n" +
                "        \"@type\" : [ \"http://etl.linkedpipes.com/ontology/DataUnit\" ],\n" +
                "        \"http://etl.linkedpipes.com/ontology/binding\" : [ {\n" +
                "          \"@value\" : \"FilesOutput\"\n" +
                "        } ],\n" +
                "        \"http://etl.linkedpipes.com/ontology/dataPath\" : [ {\n" +
                "          \"@value\" : \"working/dataunit-001-3\"\n" +
                "        } ],\n" +
                "        \"http://etl.linkedpipes.com/ontology/debug\" : [ {\n" +
                "          \"@value\" : \"001\"\n" +
                "        } ]\n" +
                "      } ],\n" +
                "      \"@id\" : \"http://localhost:8080/resources/executions/1601940398843-7-9d4c5904-8df6-4ccb-93f0-ab66a2c0cafd\"\n" +
                "    } ]"
    }
}
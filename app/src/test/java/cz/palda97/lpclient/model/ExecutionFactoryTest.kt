package cz.palda97.lpclient.model

import cz.palda97.lpclient.PowerMockTest
import cz.palda97.lpclient.model.entities.execution.Execution
import cz.palda97.lpclient.model.entities.execution.ExecutionFactory
import cz.palda97.lpclient.model.entities.execution.ExecutionStatus
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.*
import org.junit.Test

import org.junit.Assert.*

class ExecutionFactoryTest
    : PowerMockTest() {

    @Test
    fun entityTest() {
        val serverWithExecutions = ExecutionFactory(
            SERVER,
            ENTITY_EXECUTION
        ).serverWithExecutions.mailContent
        assertNotNull(serverWithExecutions)
        val executions = serverWithExecutions!!.executionList
        assertEquals(1, executions.size)
        val execution = executions.first().execution
        assertEquals(1, execution.componentExecuted)
        assertEquals(2, execution.componentFinished)
        assertEquals(3, execution.componentMapped)
        assertEquals(4, execution.componentToExecute)
        assertEquals(5, execution.componentToMap)
        assertEquals(114907L, execution.size)
        val start = DateParser.toDate("2021-02-09T19:57:27.638+01:00")!!
        assertEquals(start, execution.start)
        val end = DateParser.toDate("2021-02-09T19:57:27.738+01:00")!!
        assertEquals(end, execution.end)
        assertEquals(ExecutionStatus.FAILED, execution.status)
        assertEquals(777L, execution.serverId)
        assertEquals("http://localhost:8080/resources/executions/1612897047621-1-96d64e6d-7b7d-4a76-ac9d-21c2262448e1", execution.id)
        assertEquals("1612897047621-1-96d64e6d-7b7d-4a76-ac9d-21c2262448e1", execution.idNumber)

        val executionSame = Execution("http://localhost:8080/resources/executions/1612897047621-1-96d64e6d-7b7d-4a76-ac9d-21c2262448e1", null, null, null, null, null, null, null, null, ExecutionStatus.FINISHED, 0L)
        val executionDifferent = Execution("http://localhost:8081/resources/executions/1612897047621-1-96d64e6d-7b7d-4a76-ac9d-21c2262448e1", null, null, null, null, null, null, null, null, ExecutionStatus.FINISHED, 0L)
        assertEquals(execution, executionSame)
        assertNotEquals(execution, executionDifferent)
        assertEquals(execution.hashCode(), executionSame.hashCode())
        assertNotEquals(execution.hashCode(), executionDifferent.hashCode())

        assertEquals("Test server", serverWithExecutions.server.name)
        assertNull(serverWithExecutions.executionList.first().mark)
    }

    @Test
    fun parseExecutions() {
        val executions = ExecutionFactory(
            SERVER,
            EXECUTIONS
        ).serverWithExecutions
        assertTrue(executions.isOk)
        assertEquals(2, executions.mailContent!!.executionList.size)
    }

    @Test
    fun parseTomb() {
        val executions = ExecutionFactory(
            SERVER,
            TOMBSTONE
        ).serverWithExecutions
        assertTrue(executions.isOk)
        assertEquals(0, executions.mailContent!!.executionList.size)
    }

    @Test
    fun parseTombAndExecution() {
        val executions = ExecutionFactory(
            SERVER,
            TOMBSTONE_AND_ONE_EXECUTION
        ).serverWithExecutions
        assertTrue(executions.isOk)
        assertEquals(1, executions.mailContent!!.executionList.size)
    }

    companion object {

        private val SERVER =
            ServerInstance(
                "Test server",
                "http://example.com",
                true,
                ""
            ).apply { id = 777L }

        private const val ENTITY_EXECUTION = "[\n" +
                "    {\n" +
                "        \"@graph\": [\n" +
                "            {\n" +
                "                \"@id\": \"http://etl.linkedpipes.com/metadata\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://etl.linkedpipes.com/ontology/Metadata\"\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/serverTime\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#long\",\n" +
                "                        \"@value\": \"1617536559055\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ],\n" +
                "        \"@id\": \"http://etl.linkedpipes.com/metadata\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"@graph\": [\n" +
                "            {\n" +
                "                \"@id\": \"http://localhost:8080/resources/executions/1612897047621-1-96d64e6d-7b7d-4a76-ac9d-21c2262448e1\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://etl.linkedpipes.com/ontology/Execution\"\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/execution/componentExecuted\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#int\",\n" +
                "                        \"@value\": \"1\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/execution/componentFinished\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#int\",\n" +
                "                        \"@value\": \"2\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/execution/componentMapped\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#int\",\n" +
                "                        \"@value\": \"3\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/execution/componentToExecute\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#int\",\n" +
                "                        \"@value\": \"4\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/execution/componentToMap\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#int\",\n" +
                "                        \"@value\": \"5\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/execution/end\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#dateTime\",\n" +
                "                        \"@value\": \"2021-02-09T19:57:27.738+01:00\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/execution/size\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#long\",\n" +
                "                        \"@value\": \"114907\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/execution/start\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#dateTime\",\n" +
                "                        \"@value\": \"2021-02-09T19:57:27.638+01:00\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/pipeline\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://localhost:8080/resources/pipelines/1612896982672\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/status\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://etl.linkedpipes.com/resources/status/failed\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"@id\": \"http://localhost:8080/resources/pipelines/1612896982672\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://linkedpipes.com/ontology/Pipeline\"\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/executionMetadata\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://localhost:8080/resources/pipelines/1612896982672/metadata\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://www.w3.org/2004/02/skos/core#prefLabel\": [\n" +
                "                    {\n" +
                "                        \"@value\": \"download and scp\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"@id\": \"http://localhost:8080/resources/pipelines/1612896982672/metadata\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://linkedpipes.com/ontology/ExecutionMetadata\"\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/deleteWorkingData\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#boolean\",\n" +
                "                        \"@value\": \"false\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/execution/type\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://linkedpipes.com/resources/executionType/Full\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/logPolicy\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://linkedpipes.com/ontology/log/Preserve\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/saveDebugData\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#boolean\",\n" +
                "                        \"@value\": \"true\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ],\n" +
                "        \"@id\": \"http://localhost:8080/resources/executions/1612897047621-1-96d64e6d-7b7d-4a76-ac9d-21c2262448e1/list\"\n" +
                "    }\n" +
                "]"

        private const val TOMBSTONE_AND_ONE_EXECUTION = "[\n" +
                "    {\n" +
                "        \"@graph\": [\n" +
                "            {\n" +
                "                \"@id\": \"http://etl.linkedpipes.com/metadata\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://etl.linkedpipes.com/ontology/Metadata\"\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/serverTime\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#long\",\n" +
                "                        \"@value\": \"1601030791660\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ],\n" +
                "        \"@id\": \"http://etl.linkedpipes.com/metadata\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"@graph\": [\n" +
                "            {\n" +
                "                \"@id\": \"http://localhost:8080/resources/executions/1601030758827-0-fccd2a4e-4af3-4c61-9c0f-3086f69f7554\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://linkedpipes.com/ontology/Tombstone\"\n" +
                "                ]\n" +
                "            }\n" +
                "        ],\n" +
                "        \"@id\": \"http://localhost:8080/resources/executions/1601030758827-0-fccd2a4e-4af3-4c61-9c0f-3086f69f7554/list\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"@graph\": [\n" +
                "            {\n" +
                "                \"@id\": \"http://localhost:8080/resources/executions/1601030766166-1-39f72036-49f4-407b-89ed-c10a55fb2af8\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://etl.linkedpipes.com/ontology/Execution\"\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/execution/componentExecuted\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#int\",\n" +
                "                        \"@value\": \"0\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/execution/componentFinished\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#int\",\n" +
                "                        \"@value\": \"0\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/execution/componentMapped\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#int\",\n" +
                "                        \"@value\": \"0\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/execution/componentToExecute\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#int\",\n" +
                "                        \"@value\": \"1\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/execution/componentToMap\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#int\",\n" +
                "                        \"@value\": \"0\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/execution/end\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#dateTime\",\n" +
                "                        \"@value\": \"2020-09-25T12:46:06.337+02:00\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/execution/size\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#long\",\n" +
                "                        \"@value\": \"63066\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/execution/start\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#dateTime\",\n" +
                "                        \"@value\": \"2020-09-25T12:46:06.196+02:00\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/pipeline\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://localhost:8080/resources/pipelines/1600968747469\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/status\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://etl.linkedpipes.com/resources/status/failed\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"@id\": \"http://localhost:8080/resources/pipelines/1600968747469\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://linkedpipes.com/ontology/Pipeline\"\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/executionMetadata\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://localhost:8080/resources/pipelines/1600968747469/metadata\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://www.w3.org/2004/02/skos/core#prefLabel\": [\n" +
                "                    {\n" +
                "                        \"@value\": \"Crab \uD83E\uDD80\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"@id\": \"http://localhost:8080/resources/pipelines/1600968747469/metadata\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://linkedpipes.com/ontology/ExecutionMetadata\"\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/deleteWorkingData\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#boolean\",\n" +
                "                        \"@value\": \"false\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/execution/type\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://linkedpipes.com/resources/executionType/Full\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/logPolicy\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://linkedpipes.com/ontology/log/Preserve\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/saveDebugData\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#boolean\",\n" +
                "                        \"@value\": \"true\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ],\n" +
                "        \"@id\": \"http://localhost:8080/resources/executions/1601030766166-1-39f72036-49f4-407b-89ed-c10a55fb2af8/list\"\n" +
                "    }\n" +
                "]"

        private const val TOMBSTONE = "[\n" +
                "    {\n" +
                "        \"@graph\": [\n" +
                "            {\n" +
                "                \"@id\": \"http://etl.linkedpipes.com/metadata\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://etl.linkedpipes.com/ontology/Metadata\"\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/serverTime\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#long\",\n" +
                "                        \"@value\": \"1600969553273\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ],\n" +
                "        \"@id\": \"http://etl.linkedpipes.com/metadata\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"@graph\": [\n" +
                "            {\n" +
                "                \"@id\": \"http://localhost:8080/resources/executions/1600968897363-0-f9e2932a-0923-4367-b66f-1939217312a5\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://linkedpipes.com/ontology/Tombstone\"\n" +
                "                ]\n" +
                "            }\n" +
                "        ],\n" +
                "        \"@id\": \"http://localhost:8080/resources/executions/1600968897363-0-f9e2932a-0923-4367-b66f-1939217312a5/list\"\n" +
                "    }\n" +
                "]"

        private const val EXECUTIONS = "[\n" +
                "    {\n" +
                "        \"@graph\": [\n" +
                "            {\n" +
                "                \"@id\": \"http://etl.linkedpipes.com/metadata\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://etl.linkedpipes.com/ontology/Metadata\"\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/serverTime\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#long\",\n" +
                "                        \"@value\": \"1600943167175\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ],\n" +
                "        \"@id\": \"http://etl.linkedpipes.com/metadata\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"@graph\": [\n" +
                "            {\n" +
                "                \"@id\": \"http://localhost:8080/resources/executions/1592268569768-0-7758f9d6-8789-45ee-b40a-8ee273b45cab\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://etl.linkedpipes.com/ontology/Execution\"\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/execution/componentExecuted\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#int\",\n" +
                "                        \"@value\": \"0\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/execution/componentFinished\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#int\",\n" +
                "                        \"@value\": \"0\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/execution/componentMapped\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#int\",\n" +
                "                        \"@value\": \"0\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/execution/componentToExecute\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#int\",\n" +
                "                        \"@value\": \"0\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/execution/componentToMap\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#int\",\n" +
                "                        \"@value\": \"0\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/execution/end\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#dateTime\",\n" +
                "                        \"@value\": \"2020-06-16T02:49:31.192+02:00\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/execution/size\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#long\",\n" +
                "                        \"@value\": \"30082\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/execution/start\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#dateTime\",\n" +
                "                        \"@value\": \"2020-06-16T02:49:30.516+02:00\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/pipeline\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://localhost:8080/resources/pipelines/1583081098540\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/status\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://etl.linkedpipes.com/resources/status/finished\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"@id\": \"http://localhost:8080/resources/pipelines/1583081098540\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://linkedpipes.com/ontology/Pipeline\"\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/executionMetadata\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://localhost:8080/resources/pipelines/1583081098540/metadata\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://www.w3.org/2004/02/skos/core#prefLabel\": [\n" +
                "                    {\n" +
                "                        \"@value\": \"emptyPipe\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"@id\": \"http://localhost:8080/resources/pipelines/1583081098540/metadata\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://linkedpipes.com/ontology/ExecutionMetadata\"\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/deleteWorkingData\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#boolean\",\n" +
                "                        \"@value\": \"false\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/execution/type\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://linkedpipes.com/resources/executionType/Full\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/logPolicy\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://linkedpipes.com/ontology/log/Preserve\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/saveDebugData\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#boolean\",\n" +
                "                        \"@value\": \"true\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ],\n" +
                "        \"@id\": \"http://localhost:8080/resources/executions/1592268569768-0-7758f9d6-8789-45ee-b40a-8ee273b45cab/list\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"@graph\": [\n" +
                "            {\n" +
                "                \"@id\": \"http://localhost:8080/resources/executions/1600020082215-3-fa51778a-db09-434c-ba04-6fb00c6c3b20\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://etl.linkedpipes.com/ontology/Execution\"\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/execution/componentExecuted\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#int\",\n" +
                "                        \"@value\": \"0\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/execution/componentFinished\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#int\",\n" +
                "                        \"@value\": \"0\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/execution/componentMapped\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#int\",\n" +
                "                        \"@value\": \"0\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/execution/componentToExecute\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#int\",\n" +
                "                        \"@value\": \"1\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/execution/componentToMap\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#int\",\n" +
                "                        \"@value\": \"0\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/execution/end\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#dateTime\",\n" +
                "                        \"@value\": \"2020-09-13T20:01:35.813+02:00\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/execution/size\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#long\",\n" +
                "                        \"@value\": \"70932\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/execution/start\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#dateTime\",\n" +
                "                        \"@value\": \"2020-09-13T20:01:22.949+02:00\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/pipeline\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://localhost:8080/resources/pipelines/1599614705295\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://etl.linkedpipes.com/ontology/status\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://etl.linkedpipes.com/resources/status/failed\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"@id\": \"http://localhost:8080/resources/pipelines/1599614705295\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://linkedpipes.com/ontology/Pipeline\"\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/executionMetadata\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://localhost:8080/resources/pipelines/1599614705295/metadata\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://www.w3.org/2004/02/skos/core#prefLabel\": [\n" +
                "                    {\n" +
                "                        \"@value\": \"crab \uD83E\uDD80\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"@id\": \"http://localhost:8080/resources/pipelines/1599614705295/metadata\",\n" +
                "                \"@type\": [\n" +
                "                    \"http://linkedpipes.com/ontology/ExecutionMetadata\"\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/deleteWorkingData\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#boolean\",\n" +
                "                        \"@value\": \"false\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/execution/type\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://linkedpipes.com/resources/executionType/Full\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/logPolicy\": [\n" +
                "                    {\n" +
                "                        \"@id\": \"http://linkedpipes.com/ontology/log/Preserve\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"http://linkedpipes.com/ontology/saveDebugData\": [\n" +
                "                    {\n" +
                "                        \"@type\": \"http://www.w3.org/2001/XMLSchema#boolean\",\n" +
                "                        \"@value\": \"true\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ],\n" +
                "        \"@id\": \"http://localhost:8080/resources/executions/1600020082215-3-fa51778a-db09-434c-ba04-6fb00c6c3b20/list\"\n" +
                "    }\n" +
                "]"
    }
}
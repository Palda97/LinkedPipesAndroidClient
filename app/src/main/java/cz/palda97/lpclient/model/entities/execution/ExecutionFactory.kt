package cz.palda97.lpclient.model.entities.execution

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.*
import cz.palda97.lpclient.model.entities.pipelineview.PipelineViewFactory
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.model.travelobjects.CommonFunctions
import cz.palda97.lpclient.model.travelobjects.LdConstants
import java.lang.NumberFormatException

/**
 * Factory for transforming jsonLd to either [Execution] or execution list.
 * @param json JsonLd with executions.
 */
class ExecutionFactory(private val json: String?) {

    /**
     * Parses execution from the execution list jsonLd.
     * @param server Server that belongs to the executions.
     * @return MailPackage with server and parsed executions and tombstones.
     */
    fun parseListFromJson(server: ServerInstance): MailPackage<Pair<ServerWithExecutions, List<String>>> = fromJson(server, json)

    /**
     * Parses an execution from the execution overview jsonLd.
     * @return [Execution] or null if error.
     */
    fun parseFromOverview(serverId: Long, pipelineName: String, pipelineId: String): Execution? {
        val overview = try {
            Gson().fromJson(json, ExecutionOverview::class.java)
        } catch (e: JsonSyntaxException) {
            null
        } ?: return null
        return overview.execution(serverId, pipelineName, pipelineId)
    }

    /**
     * Parses an execution from the execution overview jsonLd.
     * @return [Execution] or null if error.
     */
    fun parseFromOverview(execution: Execution) = parseFromOverview(execution.serverId, execution.pipelineName, execution.pipelineId)

    companion object {
        private val l = Injector.generateLogFunction(this)

        private fun fromJson(
            server: ServerInstance,
            string: String?
        ): MailPackage<Pair<ServerWithExecutions, List<String>>> {
            val rootArrayList = when(val res = CommonFunctions.getRootArrayList(string)) {
                is Either.Left -> return MailPackage.brokenPackage(res.value)
                is Either.Right -> res.value
            }
            val executions = mutableListOf<Execution>()
            val tombstones = mutableListOf<String>()
            rootArrayList.forEach {
                val objectRoot = CommonFunctions.prepareSemiRootElement(it) ?: return MailPackage.brokenPackage("no graph")
                if (objectRoot.size == 1) {
                    val map = objectRoot.first() as? Map<*, *> ?: return MailPackage.brokenPackage("no map inside graph")
                    when(CommonFunctions.giveMeThatType(map)) {
                        LdConstants.TYPE_METADATA -> {
                            val time = CommonFunctions.giveMeThatString(map, LdConstants.SERVER_TIME, LdConstants.VALUE)?.toLongOrNull() ?: return MailPackage.brokenPackage("metadata with no time")
                            server.changedSince = time
                        }
                        LdConstants.TYPE_TOMBSTONE -> {
                            val tombstone = CommonFunctions.giveMeThatId(map) ?: return MailPackage.brokenPackage("tombstone with no id")
                            tombstones.add(tombstone)
                        }
                        else -> return MailPackage.brokenPackage("unknown type")
                    }
                } else {
                    val execution = when(val res = parseExecution(objectRoot, server)) {
                        is Either.Left -> return MailPackage.brokenPackage(res.value)
                        is Either.Right -> res.value
                    }
                    executions.add(execution)
                }
            }
            return MailPackage(ServerWithExecutions(server, executions) to tombstones)
        }

        private fun parseExecution(
            executionRoot: ArrayList<*>,
            server: ServerInstance
        ): Either<String, Execution> {
            val executionRootMap =
                executionRoot[0] as? Map<*, *> ?: return Either.Left(
                    "executionRoot is not Map"
                )

            if (CommonFunctions.giveMeThatType(executionRootMap) == LdConstants.TYPE_TOMBSTONE)
                return Either.Left(LdConstants.TYPE_TOMBSTONE)

            val pipelineRootMap =
                executionRoot[1] as? Map<*, *> ?: return Either.Left(
                    "pipelineRoot is not Map"
                )

            val pipeline = PipelineViewFactory.makePipelineView(pipelineRootMap, server) ?: return Either.Left("included pipeline is broken")

            return try {
                val execution =
                    makeExecution(
                        executionRootMap,
                        server
                    )
                        ?: return Either.Left("execution is null")
                Either.Right(execution.apply {
                    pipelineId = pipeline.id
                    pipelineName = pipeline.prefLabel
                })
            }catch (e: NumberFormatException) {
                Either.Left("number format exception")
            }
        }

        /**
         * Can throw number format exception!
         */
        private fun makeExecution(
            map: Map<*, *>,
            server: ServerInstance
        ): Execution? {
            val id = CommonFunctions.giveMeThatId(map) ?: return null
            val componentExecuted = CommonFunctions.giveMeThatString(map, LdConstants.COMPONENT_EXECUTED, LdConstants.VALUE)
            val componentFinished = CommonFunctions.giveMeThatString(map, LdConstants.COMPONENT_FINISHED, LdConstants.VALUE)
            val componentMapped = CommonFunctions.giveMeThatString(map, LdConstants.COMPONENT_MAPPED, LdConstants.VALUE)
            val componentToExecute = CommonFunctions.giveMeThatString(map, LdConstants.COMPONENT_TO_EXECUTE, LdConstants.VALUE)
            val componentToMap = CommonFunctions.giveMeThatString(map, LdConstants.COMPONENT_TO_MAP, LdConstants.VALUE)
            val end = CommonFunctions.giveMeThatString(map, LdConstants.EXECUTION_END, LdConstants.VALUE)
            val size = CommonFunctions.giveMeThatString(map, LdConstants.EXECUTION_SIZE, LdConstants.VALUE)
            val start = CommonFunctions.giveMeThatString(map, LdConstants.EXECUTION_START, LdConstants.VALUE)
            //val pipeline = CommonFunctions.giveMeThatString(map, LdConstants.EXECUTION_PIPELINE, LdConstants.ID) ?: return null
            val status = CommonFunctions.giveMeThatString(map, LdConstants.EXECUTION_STATUS, LdConstants.ID) ?: return null

            return Execution(
                id,
                componentExecuted?.toInt(),
                componentFinished?.toInt(),
                componentMapped?.toInt(),
                componentToExecute?.toInt(),
                componentToMap?.toInt(),
                DateParser.toDate(end),
                size?.toLong(),
                DateParser.toDate(start),
                ExecutionStatusUtilities.fromString(
                    status
                ) ?: return null,
                server.id
            )
        }
    }
}
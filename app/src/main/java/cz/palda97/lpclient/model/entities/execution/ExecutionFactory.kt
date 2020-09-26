package cz.palda97.lpclient.model.entities.execution

import android.util.Log
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.*
import cz.palda97.lpclient.model.entities.pipeline.PipelineViewFactory
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.model.travelobjects.CommonFunctions
import cz.palda97.lpclient.model.travelobjects.LdConstants
import java.lang.NumberFormatException

class ExecutionFactory(val serverWithExecutions: MailPackage<ServerWithExecutions>) {
    constructor(server: ServerInstance, string: String?) : this(
        fromJson(
            server,
            string
        )
    )

    companion object {
        private val TAG = Injector.tag(this)
        private fun l(msg: String) = Log.d(TAG, msg)

        private fun fromJson(
            server: ServerInstance,
            string: String?
        ): MailPackage<ServerWithExecutions> {
            return when (val res = CommonFunctions.getRootArrayList(string)) {
                is Either.Left -> MailPackage.brokenPackage(
                    res.value
                )
                is Either.Right -> {
                    val list = mutableListOf<Execution>()
                    res.value.forEachIndexed { index, it ->
                        if (index != 0) {
                            val resExe =
                                parseExecution(
                                    it,
                                    server
                                )
                            if (resExe is Either.Right) {
                                list.add(resExe.value)
                            }
                            else {
                                if (resExe is Either.Left && resExe.value != LdConstants.TYPE_TOMBSTONE)
                                    return MailPackage.brokenPackage(
                                        "some execution is null"
                                    )
                            }
                        }
                    }
                    MailPackage(
                        ServerWithExecutions(
                            server,
                            list
                        )
                    )
                }
            }
        }

        private fun parseExecution(
            jsonObject: Any?,
            server: ServerInstance
        ): Either<String, Execution> {
            val executionRoot = CommonFunctions.prepareSemiRootElement(jsonObject)
                ?: return Either.Left("execution is weird")

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
            l("makeExecution")
            val id = CommonFunctions.giveMeThatId(map) ?: return null
            val componentExecuted = CommonFunctions.giveMeThatString(map, LdConstants.COMPONENT_EXECUTED, LdConstants.VALUE)
            val componentFinished = CommonFunctions.giveMeThatString(map, LdConstants.COMPONENT_FINISHED, LdConstants.VALUE)
            val componentMapped = CommonFunctions.giveMeThatString(map, LdConstants.COMPONENT_MAPPED, LdConstants.VALUE)
            val componentToExecute = CommonFunctions.giveMeThatString(map, LdConstants.COMPONENT_TO_EXECUTE, LdConstants.VALUE)
            val componentToMap = CommonFunctions.giveMeThatString(map, LdConstants.COMPONENT_TO_MAP, LdConstants.VALUE)
            val end = CommonFunctions.giveMeThatString(map, LdConstants.EXECUTION_END, LdConstants.VALUE)
            val size = CommonFunctions.giveMeThatString(map, LdConstants.EXECUTION_SIZE, LdConstants.VALUE)
            val start = CommonFunctions.giveMeThatString(map, LdConstants.EXECUTION_START, LdConstants.VALUE)
            val pipeline = CommonFunctions.giveMeThatString(map, LdConstants.EXECUTION_PIPELINE, LdConstants.ID) ?: return null
            val status = CommonFunctions.giveMeThatString(map, LdConstants.EXECUTION_STATUS, LdConstants.ID) ?: return null

            l(status)
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
                pipeline,
                executionStatusFromString(
                    status
                ) ?: return null,
                server.id
            )
        }

        private fun executionStatusFromString(string: String): ExecutionStatus? = when(string) {
            LdConstants.EXECUTION_STATUS_FINISHED -> ExecutionStatus.FINISHED
            LdConstants.EXECUTION_STATUS_FAILED -> ExecutionStatus.FAILED
            LdConstants.EXECUTION_STATUS_RUNNING -> ExecutionStatus.RUNNING
            LdConstants.EXECUTION_STATUS_CANCELLED -> ExecutionStatus.CANCELLED
            LdConstants.EXECUTION_STATUS_DANGLING -> ExecutionStatus.DANGLING
            else -> null
        }
    }
}
package cz.palda97.lpclient.model

import android.util.Log
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.travelobjects.CommonFunctions
import cz.palda97.lpclient.model.travelobjects.LdConstants
import java.lang.NumberFormatException

class ExecutionFactory(val serverWithPipelineViews: MailPackage<ServerWithExecutions>) {
    constructor(server: ServerInstance, string: String?) : this(
        fromJson(server, string)
    )

    companion object {
        private val TAG = Injector.tag(this)
        private fun l(msg: String) = Log.d(TAG, msg)

        private fun fromJson(
            server: ServerInstance,
            string: String?
        ): MailPackage<ServerWithExecutions> {
            return when (val res = CommonFunctions.getRootArrayList(string)) {
                is Either.Left -> MailPackage.brokenPackage(res.value)
                is Either.Right -> {
                    val list = mutableListOf<Execution>()
                    res.value.forEachIndexed { index, it ->
                        if (index != 0) {
                            val resExe = parseExecution(
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
                    MailPackage(ServerWithExecutions(server, list))
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
                    "pipelineRoot is not Map"
                )

            if (CommonFunctions.giveMeThatType(executionRootMap) == LdConstants.TYPE_TOMBSTONE)
                return Either.Left(LdConstants.TYPE_TOMBSTONE)

            return try {
                val execution =
                    makeExecution(
                        executionRootMap,
                        server
                    ) ?: return Either.Left( "execution is null")
                Either.Right(execution)
            }catch (e: NumberFormatException) {
                Either.Left("number format exception")
            }
        }

        private fun makeExecution(
            map: Map<*, *>,
            server: ServerInstance
        ): Execution? {
            val id = CommonFunctions.giveMeThatId(map) ?: return null
            val componentExecuted = CommonFunctions.giveMeThatString(map, LdConstants.COMPONENT_EXECUTED, LdConstants.VALUE) ?: return null
            val componentFinished = CommonFunctions.giveMeThatString(map, LdConstants.COMPONENT_FINISHED, LdConstants.VALUE) ?: return null
            val componentMapped = CommonFunctions.giveMeThatString(map, LdConstants.COMPONENT_MAPPED, LdConstants.VALUE) ?: return null
            val componentToExecute = CommonFunctions.giveMeThatString(map, LdConstants.COMPONENT_TO_EXECUTE, LdConstants.VALUE) ?: return null
            val componentToMap = CommonFunctions.giveMeThatString(map, LdConstants.COMPONENT_TO_MAP, LdConstants.VALUE) ?: return null
            val end = CommonFunctions.giveMeThatString(map, LdConstants.EXECUTION_END, LdConstants.VALUE) ?: return null
            val size = CommonFunctions.giveMeThatString(map, LdConstants.EXECUTION_SIZE, LdConstants.VALUE) ?: return null
            val start = CommonFunctions.giveMeThatString(map, LdConstants.EXECUTION_START, LdConstants.VALUE) ?: return null
            val pipeline = CommonFunctions.giveMeThatString(map, LdConstants.EXECUTION_PIPELINE, LdConstants.ID) ?: return null
            val status = CommonFunctions.giveMeThatString(map, LdConstants.EXECUTION_STATUS, LdConstants.ID) ?: return null

            return Execution(
                id,
                componentExecuted.toInt(),
                componentFinished.toInt(),
                componentMapped.toInt(),
                componentToExecute.toInt(),
                componentToMap.toInt(),
                DateParser.toDate(end) ?: return null,
                size.toLong(),
                DateParser.toDate(start) ?: return null,
                pipeline,
                executionStatusFromString(status) ?: return null,
                server.id
            )
        }

        private fun executionStatusFromString(string: String): ExecutionStatus? = when(string) {
            LdConstants.EXECUTION_STATUS_FINISHED -> ExecutionStatus.FINISHED
            LdConstants.EXECUTION_STATUS_FAILED -> ExecutionStatus.FAILED
            LdConstants.EXECUTION_STATUS_RUNNING -> ExecutionStatus.RUNNING
            else -> null
        }
    }
}
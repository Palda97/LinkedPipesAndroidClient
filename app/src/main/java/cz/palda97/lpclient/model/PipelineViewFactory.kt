package cz.palda97.lpclient.model

import android.util.Log
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.travelobjects.CommonFunctions
import cz.palda97.lpclient.model.travelobjects.CommonFunctions.giveMeThatId
import cz.palda97.lpclient.model.travelobjects.CommonFunctions.giveMeThatString
import cz.palda97.lpclient.model.travelobjects.CommonFunctions.prepareSemiRootElement
import cz.palda97.lpclient.model.travelobjects.LdConstants.PREF_LABEL
import cz.palda97.lpclient.model.travelobjects.LdConstants.VALUE

class PipelineViewFactory(val serverWithPipelineViews: MailPackage<ServerWithPipelineViews>) {

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
        ): MailPackage<ServerWithPipelineViews> {
            when (val res = CommonFunctions.getRootArrayList(string)) {
                is Either.Left -> return MailPackage.brokenPackage(res.value)
                is Either.Right -> {
                    val list = mutableListOf<PipelineView>()
                    res.value.forEach {
                        val resPipe = parsePipelineView(
                            it,
                            server
                        )
                        if (resPipe is Either.Right)
                            list.add(resPipe.value)
                        else
                            return MailPackage.brokenPackage(
                                "some pipelineView is null"
                            )
                    }
                    return MailPackage(ServerWithPipelineViews(server, list))
                }
            }
        }

        private fun parsePipelineView(
            jsonObject: Any?,
            server: ServerInstance
        ): Either<String, PipelineView> {
            val pipelineRoot = prepareSemiRootElement(jsonObject)
                ?: return Either.Left("pipelineRoot is weird")
            if (pipelineRoot.size != 1)
                return Either.Left("pipelineRoot.size != 1")
            val pipelineRootMap =
                pipelineRoot[0] as? Map<*, *> ?: return Either.Left(
                    "pipelineRoot is not Map"
                )
            val pipelineView =
                makePipelineView(
                    pipelineRootMap,
                    server
                ) ?: return Either.Left(
                    "pipelineView is null"
                )
            return Either.Right(pipelineView)
        }

        private fun makePipelineView(
            map: Map<*, *>,
            server: ServerInstance
        ): PipelineView? {
            val id = giveMeThatId(map) ?: return null
            val prefLabel = giveMeThatString(map, PREF_LABEL, VALUE) ?: return null
            return PipelineView(prefLabel, id, server.id)
        }
    }
}